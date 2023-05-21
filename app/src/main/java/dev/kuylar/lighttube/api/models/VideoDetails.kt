package dev.kuylar.lighttube.api.models

import android.os.Bundle
import com.google.android.exoplayer2.MediaMetadata

class VideoDetails(
	val id: String,
	val title: String,
	val author: LightTubeChannel,
	val keywords: List<String>,
	val shortDescription: String,
	val length: String,
	val isLive: Boolean,
	val viewCount: Long,
	val allowRatings: Boolean
) {
	fun getMediaMetadata(formats: ArrayList<Format>): MediaMetadata {
		return MediaMetadata.Builder().apply {
			setTitle(title)
			setArtist(author.title)
			setDescription(shortDescription)
			setMediaType(MediaMetadata.MEDIA_TYPE_VIDEO)
			if (!isLive) {
				val b = Bundle()
				b.putString("fallback", formats.last().url)
				setExtras(b)
			}
		}.build()
	}
}
