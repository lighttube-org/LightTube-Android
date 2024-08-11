package dev.kuylar.lighttube.api.models

class Format(
	val itag: String,
	val url: String,
	val mime: String,
	val bitrate: Long,
	val width: Long? = null,
	val height: Long? = null,
	val initRange: Range? = null,
	val indexRange: Range? = null,
	val lastModified: Long,
	val contentLength: Long? = null,
	val quality: String,
	val fps: Long? = null,
	val qualityLabel: String? = null,
	val audioTrack: AudioTrack? = null,
	val averageBitrate: Int,
)
