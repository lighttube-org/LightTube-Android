package dev.kuylar.lighttube.api.models

import dev.kuylar.lighttube.api.models.renderers.IRendererData
import dev.kuylar.lighttube.api.models.renderers.RendererContainer
import java.util.Date

data class LightTubeVideo(
	val id: String,
	val title: String,
	val description: String,
	val dateText: String,
	val publishDate: Date,
	val publishType: Int,
	val viewCountText: String,
	val viewCount: Long,
	val likeCountText: String,
	val likeCount: Long,
	val channel: Channel,
	val commentCountText: String?,
	val commentCount: Int?,
	val commentsErrorMessage: String,
	val recommended: ArrayList<RendererContainer>,
	val playlist: LightTubeVideoPlaylist? = null,
	val chapters: ArrayList<VideoChapter>
): IRendererData {
	var showCommentsButton: Boolean = false
	var firstComment: Triple<String, String, Int>? = null
	fun getAsRenderer() : RendererContainer {
		return RendererContainer(
			"slimVideoInfoRenderer",
			"slimVideoInfoRenderer",
			this
		)
	}
}