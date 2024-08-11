package dev.kuylar.lighttube.api.models

import com.google.gson.annotations.SerializedName
import dev.kuylar.lighttube.api.models.renderers.IRendererData
import dev.kuylar.lighttube.api.models.renderers.RendererContainer
import java.util.Date

data class LightTubeVideo(
	val id: String,
	val title: String,
	val description: String,
	val dateText: String,
	val publishDate: Date,
	val publishType: VideoUploadType,
	val viewCountText: String,
	val viewCount: Long,
	val likeCountText: String,
	val likeCount: Long,
	val channel: Channel,
	val commentsCountText: String?,
	val commentsCount: Int?,
	val commentsErrorMessage: String?,
	val recommended: ArrayList<RendererContainer>,
	val playlist: LightTubeVideoPlaylist? = null,
	val chapters: ArrayList<VideoChapter>
): IRendererData {
	enum class VideoUploadType {
		@SerializedName("Published") Published,
		@SerializedName("Premiered") Premiered,
		@SerializedName("Streamed") Streamed,
		@SerializedName("Streaming") Streaming,
		@SerializedName("FuturePremiere") FuturePremiere,
		@SerializedName("ScheduledStream") ScheduledStream
	}

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