package dev.kuylar.lighttube.api

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Date

class UtilityApi {
	companion object {
		private var refreshToken: String = ""
		private var accessToken: String = ""
		private var tokenExpiryTimestamp: Long = 0

		fun getInstances(): ArrayList<LightTubeInstance> {
			val request: Request = Request.Builder()
				.url("https://raw.githubusercontent.com/kuylar/lighttube/master/public_instances.json")
				.build()

			OkHttpClient().newCall(request).execute().use { response ->
				return Gson().fromJson(
					response.body!!.string(),
					object : TypeToken<ArrayList<LightTubeInstance>>() {}.type
				)
			}
		}

		@Throws(Exception::class)
		fun getToken(host: String, refreshToken: String): String {
			refreshIfNeeded(host, refreshToken)
			return accessToken
		}

		@Throws(Exception::class)
		fun authorizeToken(host: String, refreshToken: String, redirectUri: String): String {
			this.refreshToken = refreshToken
			authorize(host, redirectUri)
			return accessToken
		}

		@Throws(Exception::class)
		private fun refreshIfNeeded(host: String, refreshToken: String) {
			if (tokenExpiryTimestamp >= System.currentTimeMillis() / 1000) return

			val body = FormBody.Builder().apply {
				add("grant_type", "refresh_token")
				add("refresh_token", refreshToken)
				add("client_id", "LightTube Android")
				add("client_secret", "")
			}.build()

			val request: Request = Request.Builder()
				.url("$host/oauth2/token")
				.post(body)
				.build()


			OkHttpClient().newCall(request).execute().use { response ->
				val res = Gson().fromJson(
					response.body!!.string(),
					JsonObject::class.java
				).asJsonObject

				if (res.has("error"))
					throw Exception("An error occurred while refreshing token: [${res["error"]}] ${res["error_description"]}")

				tokenExpiryTimestamp =
					(System.currentTimeMillis() / 1000) + res.getAsJsonPrimitive("expires_in").asLong
				accessToken = res.getAsJsonPrimitive("access_token").asString
				Log.d(
					"UtilityApi",
					"Refreshed token ${this.refreshToken.take(5)}...${this.refreshToken.takeLast(5)}. It will expire at ${Date(tokenExpiryTimestamp)}"
				)
			}
		}

		@Throws(Exception::class)
		private fun authorize(host: String, redirectUri: String) {
			val body = FormBody.Builder().apply {
				add("grant_type", "authorization_code")
				add("code", refreshToken)
				add("client_id", "LightTube Android")
				add("redirect_uri", redirectUri)
			}.build()

			val request: Request = Request.Builder()
				.url("$host/oauth2/token")
				.post(body)
				.build()

			OkHttpClient().newCall(request).execute().use { response ->
				val res = Gson().fromJson(
					response.body!!.string(),
					JsonObject::class.java
				).asJsonObject

				if (res.has("error"))
					throw Exception("An error occurred while refreshing token: [${res["error"]}] ${res["error_description"]}")

				tokenExpiryTimestamp =
					(System.currentTimeMillis() / 1000) + res.getAsJsonPrimitive("expires_in").asLong
				accessToken = res.getAsJsonPrimitive("access_token").asString
				Log.d(
					"UtilityApi",
					"Refreshed token ${refreshToken.take(5)}...${refreshToken.takeLast(5)}. It will expire at $tokenExpiryTimestamp"
				)
			}
		}
	}
}

class LightTubeInstance(
	val host: String,
	val api: Boolean,
	val accounts: Boolean
)