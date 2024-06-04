package dev.kuylar.lighttube.gson.serialization

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class MotdDeserializer: JsonDeserializer<List<String>> {
	override fun deserialize(
		json: JsonElement?,
		typeOfT: Type?,
		context: JsonDeserializationContext?
	): List<String> {
		if (json == null) return listOf("")

		return if (json.isJsonPrimitive && json.asJsonPrimitive.isString) {
			listOf(json.asJsonPrimitive.asString)
		} else if (json.isJsonArray) {
			json.asJsonArray.map { it.asString }
		} else {
			throw Exception("Failed to deserialize MOTD: Unknown type for ${json.asString}")
		}
	}
}