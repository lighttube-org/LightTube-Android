package dev.kuylar.lighttube.api.models

import com.github.vkay94.timebar.YouTubeChapter

class VideoChapter(
	override var title: String?,
	val thumbnails: List<LightTubeImage>,
	val timeRangeStartMillis: Long
) : YouTubeChapter {
	override val startTimeMs: Long
		get() = timeRangeStartMillis
}
