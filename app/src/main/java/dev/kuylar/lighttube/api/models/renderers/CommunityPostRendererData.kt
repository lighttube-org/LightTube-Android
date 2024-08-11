package dev.kuylar.lighttube.api.models.renderers

import dev.kuylar.lighttube.api.models.Channel

data class CommunityPostRendererData(
	val postId: String,
	val author: Channel,
	val content: String,
	val likeCountText: String,
	val likeCount: Long,
	val commentsCountText: String,
	val commentCount: Long,
	val publishedText: String?,
	val relativePublishedDate: String,
	val attachment: RendererContainer?
) : IRendererData