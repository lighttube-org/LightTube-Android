package dev.kuylar.lighttube.downloads

data class DownloadInfo(
	val id: String,
	val title: String,
	val description: String,
	val views: String,
	val uploadDate: String,
	val channelId: String,
	val channelAvatar: String,
	val channelName: String,
	val channelSubscribers: String,
	val likes: String
) {
	var downloadTimeMs: Long = 0
	var size: Long = -1
	var state: Int = -1
	var progress: Float = 0f
}