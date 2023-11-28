package dev.kuylar.lighttube.api.models

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

class LightTubeVideo(
	val id: String,
	val title: String,
	val description: String,
	val dateText: String,
	val viewCount: String,
	val likeCount: String,
	val channel: Channel,
	val commentsContinuation: String,
	val commentCount: String,
	val recommended: ArrayList<JsonObject>,
	val playlist: Any? = null,
	val chapters: ArrayList<VideoChapter>
) {
	var showCommentsButton: Boolean = false
	var firstComment: Pair<String, String>? = null
	fun getAsRenderer() : JsonObject {
		val gson = Gson()
		val asJson = gson.fromJson(gson.toJson(this), JsonObject::class.java)
		asJson.add("type", JsonPrimitive("slimVideoInfoRenderer"))
		return asJson
	}
}