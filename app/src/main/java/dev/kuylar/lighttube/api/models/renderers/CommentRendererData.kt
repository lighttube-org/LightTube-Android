package dev.kuylar.lighttube.api.models.renderers

import dev.kuylar.lighttube.api.models.Channel

data class CommentRendererData(
	val id: String,
	val content: String,
	val publishedTimeText: String,
	val relativePublishedDate: String,
	val owner: Channel,
	val likeCountText: String,
	val likeCount: Long,
	val replyCountText: String,
	val replyCount: Long,
	val loved: HeartInfo,
	val pinned: Boolean,
	val authorIsChannelOwner: Boolean,
	val replyContinuation: String,
): IRendererData {
	data class HeartInfo(
		val heartedBy: String,
		val heartedAvatarUrl: String,
	)
}
