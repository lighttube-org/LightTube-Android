package dev.kuylar.lighttube.api.models

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

class LightTubePlaylist(
	val id: String,
	val alerts: List<String>,
	val title: String,
	val description: String? = null,
	val badges: List<LightTubeBadge>,
	val channel: Channel,
	val thumbnails: List<LightTubeImage>,
	val lastUpdated: String,
	val videoCountText: String,
	val viewCountText: String,
	val continuation: String? = null,
	val videos: List<JsonObject>
) {
	fun getAsRenderer() : JsonObject {
		val gson = Gson()
		val asJson = gson.fromJson(gson.toJson(this), JsonObject::class.java)
		asJson.add("type", JsonPrimitive("playlistInfoRenderer"))
		return asJson
	}
}