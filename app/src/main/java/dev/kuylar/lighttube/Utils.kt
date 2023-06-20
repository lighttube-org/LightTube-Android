package dev.kuylar.lighttube

import com.google.gson.JsonArray
import dev.kuylar.lighttube.api.models.LightTubeImage

class Utils {
	companion object {
		fun getBestImageUrl(images: List<LightTubeImage>): String {
			return if (images.isNotEmpty())
				images.maxBy { it.height }.url
			else
				""
		}

		fun getBestImageUrlJson(images: JsonArray): String {
			return images.maxBy { it.asJsonObject.getAsJsonPrimitive("height").asInt }.asJsonObject.getAsJsonPrimitive(
				"url"
			).asString!!
		}
	}
}