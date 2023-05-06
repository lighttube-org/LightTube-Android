package dev.kuylar.lighttube.api.models

import com.google.gson.JsonObject

class LightTubeVideo(
	val id: String,
	val title: String,
	val description: String,
	val dateText: String,
	val viewCount: String,
	val likeCount: String,
	val channel: LightTubeChannel,
	val commentsContinuation: String,
	val commentCount: String,
	val recommended: ArrayList<JsonObject>,
	val playlist: Any? = null
)