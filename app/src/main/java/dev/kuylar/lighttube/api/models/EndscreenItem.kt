package dev.kuylar.lighttube.api.models

class EndscreenItem(
	val type: Long,
	val target: String,
	val title: String,
	val image: ArrayList<LightTubeImage>,
	val metadata: String,
	val style: String,
	val startMS: Long,
	val endMS: Long,
	val aspectRatio: Double,
	val left: Double,
	val top: Double,
	val width: Double
)