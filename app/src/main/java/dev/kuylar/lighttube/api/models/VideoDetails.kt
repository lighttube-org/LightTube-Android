package dev.kuylar.lighttube.api.models

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
	fun getMediaMetadata(apiHost: String): MediaMetadata {
		return MediaMetadata.Builder().apply {
			setTitle(title)
			setArtist(author.title)
			setDescription(shortDescription)
			setMediaType(MediaMetadata.MEDIA_TYPE_VIDEO)
		}.build()
	}
}
