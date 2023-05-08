package dev.kuylar.lighttube.ui

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.api.LightTubeApi
import dev.kuylar.lighttube.ui.activity.MainActivity
import dev.kuylar.lighttube.ui.fragment.VideoInfoFragment
import kotlin.concurrent.thread

class VideoPlayerManager(private val activity: MainActivity) : Player.Listener {
	private val playerHandler: Handler
	private val exoplayerView: StyledPlayerView = activity.findViewById(R.id.player)
	private val fullscreenPlayer: StyledPlayerView = activity.findViewById(R.id.fullscreen_player)
	private val player: ExoPlayer = ExoPlayer.Builder(activity).build()
	private val api: LightTubeApi = activity.api
	private val fragmentManager = activity.supportFragmentManager

	private val miniplayerTitle: TextView = activity.findViewById(R.id.miniplayer_video_title)
	private val miniplayerSubtitle: TextView = activity.findViewById(R.id.miniplayer_video_subtitle)
	private val miniplayerProgress: ProgressBar =
		activity.findViewById(R.id.miniplayer_progress_bar)
	private val miniplayer: BottomSheetBehavior<View> = activity.miniplayer
	private val playerControls = exoplayerView.findViewById<View>(R.id.player_controls)
	private var fullscreen = false

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
		initOnClickListeners(exoplayerView)
		initOnClickListeners(fullscreenPlayer)
	}

	private fun initOnClickListeners(view: View) {
		view.findViewById<MaterialButton>(R.id.player_play_pause).setOnClickListener {
			if (player.isPlaying) player.pause() else player.play()
		}

		view.findViewById<MaterialButton>(R.id.player_fullscreen).setOnClickListener {
			toggleFullscreen()
		}
	}

	private fun getActivePlayerView(): StyledPlayerView {
		return if (fullscreen) fullscreenPlayer else exoplayerView
	}

	fun playVideo(id: String) {
		if (player.currentMediaItem?.mediaId == id)
			miniplayer.state = BottomSheetBehavior.STATE_EXPANDED
		else {
			fragmentManager.beginTransaction().apply {
				val bundle = Bundle()
				bundle.putString("id", id)
				replace(R.id.player_video_info, VideoInfoFragment::class.java, bundle)
			}.commit()
			thread {
				val item = mediaItemFromVideoId(id)
				playerHandler.post {
					if (miniplayer.state == BottomSheetBehavior.STATE_HIDDEN)
						miniplayer.state = BottomSheetBehavior.STATE_EXPANDED
					else
						miniplayer.state = BottomSheetBehavior.STATE_COLLAPSED
					player.setMediaItem(item)
					player.prepare()
					player.play()
				}
			}
		}
	}

	private fun mediaItemFromVideoId(id: String): MediaItem {
		val video = api.getPlayer(id).data!!
		return MediaItem.Builder().apply {
			setUri("${api.host}/proxy/media/$id.m3u8?useProxy=false")
			setMediaMetadata(video.details.getMediaMetadata(api.host))
			setMediaId(id)
		}.build()
	}

	override fun onEvents(player: Player, events: Player.Events) {
		super.onEvents(player, events)

		if (events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)) {
			miniplayerTitle.text = player.currentMediaItem?.mediaMetadata?.title
			miniplayerSubtitle.text = player.currentMediaItem?.mediaMetadata?.artist
		}

		if (events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED)) {
			getActivePlayerView().keepScreenOn = !(player.playbackState == Player.STATE_IDLE ||
					player.playbackState == Player.STATE_ENDED ||
					!player.playWhenReady || !player.isPlaying)
		}

		getActivePlayerView().findViewById<MaterialButton>(R.id.player_play_pause).icon =
			AppCompatResources.getDrawable(
				activity,
				if (player.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
			)

		getActivePlayerView().findViewById<CircularProgressIndicator>(R.id.player_buffering_progress).visibility =
			if (player.playbackState == Player.STATE_BUFFERING) View.VISIBLE else View.GONE
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
		if (fullscreen) {
			fullscreenPlayer.visibility = View.GONE
			StyledPlayerView.switchTargetView(player, fullscreenPlayer, exoplayerView)
			activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
			activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
			getActivePlayerView().findViewById<MaterialButton>(R.id.player_fullscreen).icon =
				ContextCompat.getDrawable(
					activity,
					R.drawable.ic_fullscreen
				)
			fullscreen = false
		} else {
			fullscreenPlayer.visibility = View.VISIBLE
			StyledPlayerView.switchTargetView(player, exoplayerView, fullscreenPlayer)
			activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
			activity.window.decorView.systemUiVisibility = (
					View.SYSTEM_UI_FLAG_FULLSCREEN
							or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
							or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					)
			getActivePlayerView().findViewById<MaterialButton>(R.id.player_fullscreen).icon =
				ContextCompat.getDrawable(
					activity,
					R.drawable.ic_fullscreen_exit
				)
			fullscreen = true
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
}