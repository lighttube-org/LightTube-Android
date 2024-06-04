package dev.kuylar.lighttube.api.models

data class EndscreenItem(
	val type: Long,
	val title: String,
	//val image: ArrayList<LightTubeImage>, TODO: needs to be simplified
	val metadata: String,
	val startMs: Long,
	val endMs: Long,
	val aspectRatio: Double,
	val left: Double,
	val top: Double,
	val width: Double,
	val target: String
)