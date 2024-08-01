package dev.kuylar.lighttube.api.models.renderers

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class RendererSerializer : JsonDeserializer<RendererContainer> {
	override fun deserialize(
		json: JsonElement?,
		typeOfT: Type?,
		context: JsonDeserializationContext?
	): RendererContainer? {
		if (json == null) return null

		if (!json.isJsonObject) throw JsonParseException("Expected an object, but got '$json'")

		val obj = json.asJsonObject
		var type = obj.getAsJsonPrimitive("type").asString
		val originalType = obj.getAsJsonPrimitive("originalType").asString

		val renderer: IRendererData? = when (type) {
			"video" -> context?.deserialize(
				obj.getAsJsonObject("data"),
				VideoRendererData::class.java
			)

			"playlist" -> context?.deserialize(
				obj.getAsJsonObject("data"),
				PlaylistRendererData::class.java
			)

			"channel" -> context?.deserialize(
				obj.getAsJsonObject("data"),
				ChannelRendererData::class.java
			)

			"comment" -> context?.deserialize(
				obj.getAsJsonObject("data"),
				CommentRendererData::class.java
			)

			"container" -> context?.deserialize(
				obj.getAsJsonObject("data"),
				ContainerRendererData::class.java
			)

			"continuation" -> context?.deserialize(
				obj.getAsJsonObject("data"),
				ContinuationRendererData::class.java
			)

			else -> {
				type = "exception"
				ExceptionRendererData(
					"LTA: Unknown renderer type ${obj.getAsJsonPrimitive("type").asString}",
					originalType
				)
			}
		}

		return RendererContainer(
			type,
			originalType,
			renderer ?: ExceptionRendererData(
				"LTA: Renderer deserialized to null for type ${obj.getAsJsonPrimitive("type").asString}",
				originalType
			)
		)
	}
}