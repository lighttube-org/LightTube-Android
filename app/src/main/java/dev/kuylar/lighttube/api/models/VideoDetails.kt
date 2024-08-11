package dev.kuylar.lighttube.api.models

import android.os.Bundle
import androidx.media3.common.MediaMetadata
import java.net.URLEncoder
import java.util.Date

class VideoDetails(
	val id: String,
	val title: String,
	val keywords: List<String>,
	val shortDescription: String,
	val category: String,
	val isLive: Boolean,
	val isFallback: Boolean,
	val allowRatings: Boolean,
	val isFamilySafe: Boolean,
	val thumbnails: List<LightTubeImage>,
	val publishDate: Date,
	val uploadDate: Date,
	val liveStreamStartDate: Date,
	val length: String,
	val author: Channel,

	val viewCount: Long,
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
