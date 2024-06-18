package dev.kuylar.lighttube.api.models.renderers

import dev.kuylar.lighttube.api.models.Channel
import dev.kuylar.lighttube.api.models.LightTubeBadge
import dev.kuylar.lighttube.api.models.LightTubeImage
import java.util.Date

data class VideoRendererData(
	val videoId: String,
	val title: String,
	val thumbnails: List<LightTubeImage>,
	val author: Channel?,
	val duration: String,
	val publishedText: String?,
	val relativePublishedDate: String,
	val viewCountText: String?,
	val viewCount: Long,
	val badges: List<LightTubeBadge>,
	val description: String?,
	val premiereStartTime: Date?,
	val videoIndexText: String?,
	val exactPublishDate: Date?,
	val editable: Boolean?
) : IRendererData