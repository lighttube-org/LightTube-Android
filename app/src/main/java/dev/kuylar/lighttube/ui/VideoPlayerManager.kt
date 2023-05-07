package dev.kuylar.lighttube.ui

import android.os.Bundle
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.StyledPlayerView
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.api.LightTubeApi
import dev.kuylar.lighttube.ui.activity.MainActivity
import android.os.Handler
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.exoplayer2.Player
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dev.kuylar.lighttube.ui.fragment.VideoInfoFragment
import kotlin.concurrent.thread

class VideoPlayerManager(activity: MainActivity) : Player.Listener {
	private val playerHandler: Handler
	private val exoplayerView: StyledPlayerView = activity.findViewById(R.id.player)
	private val player: ExoPlayer = ExoPlayer.Builder(activity).build()
	private val api: LightTubeApi = activity.api
	private val fragmentManager = activity.supportFragmentManager
	private val miniplayerTitle: TextView = activity.findViewById(R.id.miniplayer_video_title)
	private val miniplayerSubtitle: TextView = activity.findViewById(R.id.miniplayer_video_subtitle)
	private val miniplayerProgress: ProgressBar =
		activity.findViewById(R.id.miniplayer_progress_bar)
	private val miniplayer: BottomSheetBehavior<View> = activity.miniplayer

	init {
		exoplayerView.player = player
		player.addListener(this)
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
	}

	fun stop() {
		miniplayer.state = BottomSheetBehavior.STATE_HIDDEN
		player.stop()
		player.clearMediaItems()
	}
}