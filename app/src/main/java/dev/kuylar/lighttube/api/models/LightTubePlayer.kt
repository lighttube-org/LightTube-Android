package dev.kuylar.lighttube.api.models

import java.util.Date

data class LightTubePlayer(
	val details: VideoDetails,
	val endscreen: Endscreen,
	val storyboard: Storyboard,
	val captions: ArrayList<Caption>,
	val formats: ArrayList<Format>,
	val adaptiveFormats: ArrayList<Format>,
	val expiryTimestamp: Date,
	val hlsManifestURL: String? = null,
	val dashManifestURL: String? = null
)