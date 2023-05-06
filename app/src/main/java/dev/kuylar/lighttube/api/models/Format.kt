package dev.kuylar.lighttube.api.models

class Format(
	val itag: String,
	val bitrate: Long,
	val contentLength: Long? = null,
	val fps: Long? = null,
	val height: Long? = null,
	val width: Long? = null,
	val initRange: Range,
	val indexRange: Range,
	val mimeType: String,
	val url: String,
	val quality: String,
	val qualityLabel: String? = null,
	val audioQuality: String? = null,
	val audioSampleRate: Long,
	val audioChannels: Long? = null,
	val audioTrack: AudioTrack? = null
)
