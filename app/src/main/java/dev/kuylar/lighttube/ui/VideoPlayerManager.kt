package dev.kuylar.lighttube.ui

import android.os.Bundle
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.StyledPlayerView
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.api.LightTubeApi
import dev.kuylar.lighttube.ui.activity.MainActivity
import android.os.Handler
import androidx.fragment.app.FragmentContainerView
import dev.kuylar.lighttube.ui.fragment.VideoInfoFragment
import kotlin.concurrent.thread

class VideoPlayerManager(activity: MainActivity) {
	private val exoplayerView: StyledPlayerView = activity.findViewById(R.id.player)
	private val player: ExoPlayer = ExoPlayer.Builder(activity).build()
	private val api: LightTubeApi = activity.api
	private val fragmentManager = activity.supportFragmentManager

	init {
		exoplayerView.player = player
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
}