package dev.kuylar.lighttube.api.models

import com.github.vkay94.timebar.YouTubeChapter

class VideoChapter(
	val startSeconds: Double,
	override var title: String?,
	val thumbnails: List<LightTubeImage>
) : YouTubeChapter {
	override val startTimeMs: Long
		get() = Math.round(startSeconds * 1000)
}
