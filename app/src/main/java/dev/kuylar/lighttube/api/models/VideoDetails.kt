package dev.kuylar.lighttube.api.models

import android.os.Bundle
import com.google.android.exoplayer2.MediaMetadata
import java.net.URLEncoder

class VideoDetails(
	val id: String,
	val title: String,
	val author: Channel,
	val keywords: List<String>,
	val shortDescription: String,
	val length: String,
	val isLive: Boolean,
	val viewCount: Long,
	val allowRatings: Boolean
) {
	fun getMediaMetadata(formats: ArrayList<Format>, storyboard: Storyboard): MediaMetadata {
		return MediaMetadata.Builder().apply {
			setTitle(title)
			setArtist(author.title)
			setDescription(shortDescription)
			setMediaType(MediaMetadata.MEDIA_TYPE_VIDEO)
			if (!isLive) {
				val b = Bundle()
				b.putString("fallback", formats.last().url)
				b.putLong("length", length.split(":").let {
					val hours = it[0]
					val minutes = it[1]
					val seconds = it[2]
					hours.toLong() * 3600L + minutes.toLong() * 60L + seconds.toLong()
				})
				b.putString(
					"storyboard",
					storyboard.levels.map {
						URLEncoder.encode(
							it.key,
							Charsets.UTF_8.name()
						) + "=" + URLEncoder.encode(it.value, Charsets.UTF_8.name())
					}.joinToString("&")
				)
				b.putString("recommendedLevel", storyboard.recommendedLevel.toString())
				setExtras(b)
			}
		}.build()
	}
}
