package dev.kuylar.lighttube

import com.google.gson.Gson
import com.google.gson.JsonArray
import dev.kuylar.lighttube.api.models.LightTubeImage
import okhttp3.OkHttpClient
import okhttp3.Request

class Utils {
	companion object {
		val http = OkHttpClient()
		val gson = Gson()

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

		fun getDislikeCount(videoId: String): Long {
			try {
				val req = Request.Builder().apply {
					url("https://returnyoutubedislikeapi.com/votes?videoId=$videoId")
					header(
						"User-Agent",
						"LightTube-Android/1.0 (https://github.com/kuylar/lighttube-android)"
					)
				}.build()

				http.newCall(req).execute().use { response ->
					val res = gson.fromJson(response.body!!.string(), RYDResponse::class.java)
					return res.dislikes
				}
			} catch (e: Exception) {
				return -1
			}
		}
	}
}