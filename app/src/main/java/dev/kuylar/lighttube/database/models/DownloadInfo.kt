package dev.kuylar.lighttube.database.models

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
	lateinit var path: String
	lateinit var downloadDate: String
	var size: Long = -1
	var complete: Boolean = false
	var progress: Long = 0
	var downloadId: Long = 0
}