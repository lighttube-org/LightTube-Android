package dev.kuylar.lighttube.ui

import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import com.github.vkay94.dtpv.DoubleTapPlayerView
import com.github.vkay94.dtpv.youtube.YouTubeOverlay
import com.github.vkay94.timebar.LibTimeBar
import com.github.vkay94.timebar.YouTubeChapter
import com.github.vkay94.timebar.YouTubeSegment
import com.github.vkay94.timebar.YouTubeTimeBar
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Tracks
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.SponsorBlockSegment
import dev.kuylar.lighttube.StoryboardInfo
import dev.kuylar.lighttube.Utils
import dev.kuylar.lighttube.api.LightTubeApi
import dev.kuylar.lighttube.api.models.LightTubeException
import dev.kuylar.lighttube.api.models.VideoChapter
import dev.kuylar.lighttube.ui.activity.MainActivity
import dev.kuylar.lighttube.ui.fragment.PlayerSettingsFragment
import dev.kuylar.lighttube.ui.fragment.VideoInfoFragment
import java.io.IOException
import kotlin.concurrent.thread

class VideoPlayerManager(private val activity: MainActivity) : Player.Listener,
	LibTimeBar.SegmentListener {
	private var videoTracks: Tracks? = null
	private val playerHandler: Handler
	private val exoplayerView: DoubleTapPlayerView = activity.findViewById(R.id.player)
	private val doubleTapView: YouTubeOverlay = activity.findViewById(R.id.player_overlay)
	private val fullscreenPlayer: DoubleTapPlayerView =
		activity.findViewById(R.id.fullscreen_player)
	private val fullscreenDoubleTapView: YouTubeOverlay =
		activity.findViewById(R.id.fullscreen_player_overlay)
	private val player: ExoPlayer = ExoPlayer.Builder(activity).apply {
		setHandleAudioBecomingNoisy(true)
	}.build()
	private lateinit var api: LightTubeApi
	private val fragmentManager = activity.supportFragmentManager

	private val miniplayerTitle: TextView = activity.findViewById(R.id.miniplayer_video_title)
	private val miniplayerSubtitle: TextView = activity.findViewById(R.id.miniplayer_video_subtitle)
	private val miniplayerProgress: ProgressBar =
		activity.findViewById(R.id.miniplayer_progress_bar)
	private val miniplayer: BottomSheetBehavior<View> = activity.miniplayer
	private val playerControls = exoplayerView.findViewById<View>(R.id.player_controls)
	private var fullscreen = false
	private var chapters: ArrayList<VideoChapter>? = null
	private var storyboard: StoryboardInfo? = null

	init {
		exoplayerView.player = player
		player.addListener(this)
		player.setAudioAttributes(
			AudioAttributes.Builder().apply {
				setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
				setAllowedCapturePolicy(C.ALLOW_CAPTURE_BY_ALL)
				setUsage(C.USAGE_MEDIA)
			}.build(),
			true
		)
		playerHandler = Handler(player.applicationLooper)

		api = activity.getApi()

		// im sorry for this monstrosity
		var r = Runnable {}
		r = Runnable {
			val buffering = player.playbackState == Player.STATE_BUFFERING
			miniplayerProgress.isIndeterminate = buffering
			if (buffering) {
				miniplayerProgress.max = 1
				miniplayerProgress.progress = 0
			} else {
				miniplayerProgress.max = player.duration.toInt()
				miniplayerProgress.setProgress(player.currentPosition.toInt(), true)
			}
			playerHandler.postDelayed(r, 100)
		}
		playerHandler.post(r)

		// im sorry for this monstrosity too
		forAllPlayerViews { view, isFullscreen ->
			view.findViewById<MaterialButton>(R.id.player_play_pause).setOnClickListener {
				if (player.isPlaying) player.pause() else player.play()
			}

			view.findViewById<MaterialButton>(R.id.player_fullscreen).setOnClickListener {
				toggleFullscreen()
			}

			view.findViewById<MaterialButton>(R.id.player_settings).setOnClickListener {
				PlayerSettingsFragment(player).show(fragmentManager, null)
			}

			view.findViewById<MaterialButton>(R.id.player_captions).setOnClickListener {
				if (player.currentTracks.groups.any { it.type == C.TRACK_TYPE_TEXT })
					PlayerSettingsFragment(player, "caption").show(fragmentManager, null)
			}

			view.findViewById<MaterialButton>(R.id.player_minimize).setOnClickListener {
				if (fullscreen) toggleFullscreen()
				else miniplayer.state = BottomSheetBehavior.STATE_COLLAPSED
			}

			if (!isFullscreen) {
				view.findViewById<View>(R.id.player_metadata).visibility = View.GONE
			}

			val timeBar =
				view.findViewById<YouTubeTimeBar>(com.google.android.exoplayer2.ui.R.id.exo_progress)
			timeBar.addSegmentListener(this)

			if (isFullscreen) {
				fullscreenDoubleTapView
					.player(player)
					.performListener(object : YouTubeOverlay.PerformListener {
						override fun onAnimationStart() {
							view.useController = false
							fullscreenDoubleTapView.visibility = View.VISIBLE
						}

						override fun onAnimationEnd() {
							fullscreenDoubleTapView.visibility = View.GONE
							view.useController = true
						}
					})
				view.controller(fullscreenDoubleTapView)
			} else {
				doubleTapView.player(player)
					.performListener(object : YouTubeOverlay.PerformListener {
						override fun onAnimationStart() {
							view.useController = false
							doubleTapView.visibility = View.VISIBLE
						}

						override fun onAnimationEnd() {
							doubleTapView.visibility = View.GONE
							view.useController = true
						}
					})
				view.controller(doubleTapView)
			}
		}
	}

	private fun forAllPlayerViews(runnable: ((DoubleTapPlayerView, Boolean) -> Unit)) {
		runnable.invoke(exoplayerView, false)
		runnable.invoke(fullscreenPlayer, true)
	}

	private fun setCaptionsButtonState(buttonState: Int) {
		forAllPlayerViews { playerView, _ ->
			val button =
				playerView.findViewById<MaterialButton>(R.id.player_captions)
			when (buttonState) {
				0 -> {
					button.alpha = 0.5f
					button.isEnabled = false
					button.icon =
						ContextCompat.getDrawable(activity, R.drawable.ic_captions)
				}

				1 -> {
					button.alpha = 1f
					button.isEnabled = true
					button.icon =
						ContextCompat.getDrawable(activity, R.drawable.ic_captions)
				}

				2 -> {
					button.alpha = 1f
					button.isEnabled = true
					button.icon =
						ContextCompat.getDrawable(activity, R.drawable.ic_captions_on)
				}
			}
		}
	}

	private fun getActivePlayerView(): DoubleTapPlayerView {
		return if (fullscreen) fullscreenPlayer else exoplayerView
	}

	fun playVideo(id: String) {
		if (player.currentMediaItem?.mediaId == id)
			miniplayer.state = BottomSheetBehavior.STATE_EXPANDED
		else {
			fragmentManager.beginTransaction().apply {
				replace(
					R.id.player_video_info,
					VideoInfoFragment::class.java,
					bundleOf(Pair("id", id), Pair("playlistId", null))
				)
			}.commit()
			miniplayer.state = BottomSheetBehavior.STATE_EXPANDED
			thread {
				try {
					val item = mediaItemFromVideoId(id)
					playerHandler.post {
						player.setMediaItem(item)
						player.prepare()
						player.play()
					}
				} catch (e: IOException) {
					activity.runOnUiThread {
						Toast.makeText(
							activity,
							R.string.error_connection,
							Toast.LENGTH_LONG
						).show()
					}
				} catch (e: LightTubeException) {
					activity.runOnUiThread {
						Toast.makeText(
							activity,
							activity.getString(R.string.error_lighttube, e.message),
							Toast.LENGTH_LONG
						).show()
					}
				}
			}
		}
	}

	private fun mediaItemFromVideoId(id: String): MediaItem {
		val video = api.getPlayer(id).data!!
		return MediaItem.Builder().apply {
			setUri("${api.host}/proxy/media/$id.m3u8?useProxy=false")
			setMediaMetadata(video.details.getMediaMetadata(video.formats, video.storyboard))
			setMediaId(id)
		}.build()
	}

	override fun onEvents(player: Player, events: Player.Events) {
		super.onEvents(player, events)

		if (events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)) {
			miniplayerTitle.text = player.currentMediaItem?.mediaMetadata?.title
			miniplayerSubtitle.text = player.currentMediaItem?.mediaMetadata?.artist
			exoplayerView.findViewById<TextView>(R.id.player_title).text =
				player.currentMediaItem?.mediaMetadata?.title
			exoplayerView.findViewById<TextView>(R.id.player_subtitle).text =
				player.currentMediaItem?.mediaMetadata?.artist
			fullscreenPlayer.findViewById<TextView>(R.id.player_title).text =
				player.currentMediaItem?.mediaMetadata?.title
			fullscreenPlayer.findViewById<TextView>(R.id.player_subtitle).text =
				player.currentMediaItem?.mediaMetadata?.artist
			if (player.currentMediaItem?.mediaId != null)
				setSponsors(player.currentMediaItem?.mediaId!!)
			try {
				if (player.currentMediaItem?.mediaMetadata?.extras != null)
					setStoryboards(
						player.currentMediaItem?.mediaMetadata?.extras?.getString("storyboard"),
						player.currentMediaItem?.mediaMetadata?.extras?.getString("recommendedLevel"),
						player.currentMediaItem?.mediaMetadata?.extras?.getLong("length")
					)
				else
					storyboard = null
			} catch (e: Exception) {
				storyboard = null
			}
			videoTracks = null
		}

		if (events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED)) {
			getActivePlayerView().keepScreenOn = !(player.playbackState == Player.STATE_IDLE ||
					player.playbackState == Player.STATE_ENDED ||
					!player.playWhenReady || !player.isPlaying)
		}

		if (events.contains(Player.EVENT_PLAYER_ERROR)) {
			if (player.playerError?.errorCode == 2004) {
				// fallback to muxed format
				val cmii = player.currentMediaItemIndex
				val fallback = getFallbackMediaItem(player.currentMediaItem!!)
				if (fallback != null) {
					player.addMediaItem(cmii + 1, fallback)
					player.seekToNextMediaItem()
					player.prepare()
					player.play()
					Toast.makeText(
						activity,
						R.string.error_playback_falling_back,
						Toast.LENGTH_LONG
					).show()
					player.removeMediaItem(cmii)
				} else {
					Toast.makeText(activity, R.string.error_playback, Toast.LENGTH_LONG).show()
				}
			}
		}

		if (player.currentTracks.groups.none { it.type == C.TRACK_TYPE_TEXT }) {
			setCaptionsButtonState(0)
		} else if (player.trackSelectionParameters.overrides.filter { it.key.type == C.TRACK_TYPE_TEXT }
				.isNotEmpty()) {
			setCaptionsButtonState(2)
		} else {
			setCaptionsButtonState(1)
		}

		getActivePlayerView().findViewById<MaterialButton>(R.id.player_play_pause).icon =
			AppCompatResources.getDrawable(
				activity,
				if (player.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
			)

		getActivePlayerView().findViewById<CircularProgressIndicator>(R.id.player_buffering_progress).visibility =
			if (player.playbackState == Player.STATE_BUFFERING) View.VISIBLE else View.GONE
	}

	private fun setStoryboards(levels: String?, recommendedLevel: String?, length: Long?) {
		return //FIXME: disabled until i write a working storyboard view
		// if (levels == null || length == null || recommendedLevel == null) storyboard = null
		// storyboard = StoryboardInfo(levels!!, recommendedLevel!!, length!!)
	}

	private fun setSponsors(videoId: String) {
		thread {
			val sponsors = Utils.getSponsorBlockInfo(videoId)
			activity.runOnUiThread {
				forAllPlayerViews { player, _ ->
					if (sponsors != null) {
						player.findViewById<YouTubeTimeBar>(com.google.android.exoplayer2.ui.R.id.exo_progress).segments =
							sponsors.segments
					} else {
						player.findViewById<YouTubeTimeBar>(com.google.android.exoplayer2.ui.R.id.exo_progress).segments =
							listOf(SponsorBlockSegment("", "", listOf(0.0, 0.0), "", 0.0, 0, 0, ""))
					}
				}
			}
		}
	}

	override fun onTracksChanged(tracks: Tracks) {
		videoTracks = tracks
	}

	private fun getFallbackMediaItem(currentItem: MediaItem): MediaItem? {
		return try {
			MediaItem.Builder().apply {
				setUri(currentItem.mediaMetadata.extras!!.getString("fallback"))
				Log.i(
					"LTPlayer",
					"-> URL: ${currentItem.mediaMetadata.extras!!.getString("fallback")}"
				)
				setMediaId(currentItem.mediaId)
				Log.i("LTPlayer", "-> ID: ${currentItem.mediaId}")
				setMediaMetadata(currentItem.mediaMetadata.buildUpon().apply { setExtras(null) }
					.build())
			}.build()
		} catch (e: Exception) {
			null
		}
	}

	fun stop() {
		miniplayer.state = BottomSheetBehavior.STATE_HIDDEN
		player.stop()
		player.clearMediaItems()
	}

	fun toggleControls(visible: Boolean) {
		playerControls.visibility = if (visible) View.VISIBLE else View.GONE
	}

	private fun toggleFullscreen() {
		fullscreen = if (fullscreen) {
			activity.exitFullscreen(player, exoplayerView)
			false
		} else {
			activity.enterFullscreen(player, exoplayerView, getAspectRatio() < 1)
			true
		}
	}

	private fun getAspectRatio(): Float {
		return try {
			player.videoSize.width.toFloat() / player.videoSize.height.toFloat()
		} catch (e: Exception) {
			Float.MAX_VALUE
		}
	}

	fun exitFullscreen(): Boolean {
		if (!fullscreen) return false
		toggleFullscreen()
		return true
	}

	fun setVolume(volume: Float) {
		player.volume = volume
	}

	fun closeSheets(): Boolean {
		return try {
			(fragmentManager.findFragmentById(R.id.player_video_info) as VideoInfoFragment).closeSheets()
		} catch (e: Exception) {
			false
		}
	}

	fun showCommentsButton() {
		try {
			(fragmentManager.findFragmentById(R.id.player_video_info) as VideoInfoFragment).showCommentsButton()
		} catch (e: Exception) { }
	}

	fun setSheets(details: Boolean, comments: Boolean) {
		(fragmentManager.findFragmentById(R.id.player_video_info) as VideoInfoFragment).setSheets(
			details,
			comments
		)
	}

	fun setChapters(videoId: String, chapters: ArrayList<VideoChapter>?) {
		if (videoId != player.currentMediaItem?.mediaId) return
		this.chapters = chapters
		if (chapters != null) {
			exoplayerView.findViewById<YouTubeTimeBar>(com.google.android.exoplayer2.ui.R.id.exo_progress).chapters =
				chapters
			fullscreenPlayer.findViewById<YouTubeTimeBar>(com.google.android.exoplayer2.ui.R.id.exo_progress).chapters =
				chapters
		} else {
			exoplayerView.findViewById<YouTubeTimeBar>(com.google.android.exoplayer2.ui.R.id.exo_progress).chapters =
				listOf(VideoChapter(null, emptyList(), 0))
			fullscreenPlayer.findViewById<YouTubeTimeBar>(com.google.android.exoplayer2.ui.R.id.exo_progress).chapters =
				listOf(VideoChapter(null, emptyList(), 0))
		}
	}

	override fun onChapterChanged(timeBar: LibTimeBar, newChapter: YouTubeChapter, drag: Boolean) {
		// todo: show current chapter on UI
	}

	override fun onSegmentChanged(timeBar: LibTimeBar, newSegment: YouTubeSegment?) {
		if (newSegment != null)
			Toast.makeText(
				activity,
				(newSegment as SponsorBlockSegment).actionType,
				Toast.LENGTH_LONG
			).show()
	}
}