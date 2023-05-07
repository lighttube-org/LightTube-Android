package dev.kuylar.lighttube.ui

import android.os.Bundle
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.StyledPlayerView
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.api.LightTubeApi
import dev.kuylar.lighttube.ui.activity.MainActivity
import android.os.Handler
import android.widget.TextView
import androidx.fragment.app.FragmentContainerView
import com.google.android.exoplayer2.Player
import dev.kuylar.lighttube.ui.fragment.VideoInfoFragment
import kotlin.concurrent.thread

class VideoPlayerManager(activity: MainActivity) : Player.Listener {
	private val exoplayerView: StyledPlayerView = activity.findViewById(R.id.player)
	private val player: ExoPlayer = ExoPlayer.Builder(activity).build()
	private val api: LightTubeApi = activity.api
	private val fragmentManager = activity.supportFragmentManager
	private val miniplayerTitle: TextView = activity.findViewById(R.id.miniplayer_video_title)
	private val miniplayerSubtitle: TextView = activity.findViewById(R.id.miniplayer_video_subtitle)

	init {
		exoplayerView.player = player
		player.addListener(this)
	}

	fun playVideo(id: String) {
		fragmentManager.beginTransaction().apply {
			val bundle = Bundle()
			bundle.putString("id", id)
			replace(R.id.player_video_info, VideoInfoFragment::class.java, bundle)
		}.commit()
		thread {
			val item = mediaItemFromVideoId(id)
			Handler(player.applicationLooper).post {
				player.setMediaItem(item)
				player.prepare()
				player.play()
			}
		}
	}

	private fun mediaItemFromVideoId(id: String): MediaItem {
		val video = api.getPlayer(id).data!!
		return MediaItem.Builder().apply {
			setUri("${api.host}/proxy/media/$id.m3u8?useProxy=false")
			setMediaMetadata(video.details.getMediaMetadata(api.host))
		}.build()
	}

	override fun onEvents(player: Player, events: Player.Events) {
		super.onEvents(player, events)

		if (events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)) {
			miniplayerTitle.text = player.currentMediaItem?.mediaMetadata?.title
			miniplayerSubtitle.text = player.currentMediaItem?.mediaMetadata?.artist
		}
	}
}