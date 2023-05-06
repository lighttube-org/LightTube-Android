package dev.kuylar.lighttube.api.models

class LightTubePlayer(
	val details: VideoDetails,
	val endscreen: Endscreen,
	val storyboard: Storyboard,
	val captions: ArrayList<Any?>,
	val formats: ArrayList<Format>,
	val adaptiveFormats: ArrayList<Format>,
	val expiresInSeconds: Long,
	val hlsManifestURL: String? = null,
	val dashManifestURL: String? = null
)