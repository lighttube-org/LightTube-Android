package dev.kuylar.lighttube.api

import android.app.Activity
import android.util.Log
import android.view.View
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.api.models.InstanceInfo
import dev.kuylar.lighttube.databinding.ItemInstanceBinding
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Date

class UtilityApi {
	companion object {
		private val http = OkHttpClient()
		private var refreshToken: String = ""
		private var accessToken: String = ""
		private var tokenExpiryTimestamp: Long = 0

		fun getInstances(): ArrayList<LightTubeInstance> {
			val request: Request = Request.Builder()
				.url("https://lighttube.kuylar.dev/instances")
				.build()

			http.newCall(request).execute().use { response ->
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


			http.newCall(request).execute().use { response ->
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

			http.newCall(request).execute().use { response ->
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

data class LightTubeInstance(
	val host: String,
	val country: String,
	val scheme: String,
	val isCloudflare: Boolean,
	val apiEnabled: Boolean,
	val proxyEnabled: String,
	val accountsEnabled: Boolean,
) {
	fun fillBinding(binding: ItemInstanceBinding, activity: Activity) {
		val context = binding.root.context
		val instanceInfo: InstanceInfo
		try {
			instanceInfo = LightTubeApi("$scheme://$host").getInstanceInfo()
		} catch (e: Exception) {
			binding.loading.visibility = View.GONE
			activity.runOnUiThread {
				Log.e("UtilityApi.fillBinding", "Failed to get information about instance $host")
				binding.instanceTitle.text = arrayOf(getFlag(), host).joinToString(" ")
				binding.instanceDescription.text = context.getString(
					R.string.setup_instance_load_fail,
					context.getString(R.string.setup_instance_load_fail_this)
				)
			}
			return
		}

		activity.runOnUiThread {
			binding.loading.visibility = View.GONE
			binding.instanceTitle.text = arrayOf(getFlag(), host).joinToString(" ")
			binding.instanceDescription.text = if (instanceInfo.type != "lighttube")
				context.getString(R.string.setup_instance_invalid, instanceInfo.type)
			else if (apiEnabled)
				context.getString(
					R.string.template_instance_info,
					instanceInfo.version,
					if (accountsEnabled) context.getString(R.string.enabled) else context.getString(
						R.string.disabled
					),
					if (proxyEnabled == "all") context.getString(R.string.enabled) else context.getString(
						R.string.disabled
					)
				)
			else context.getString(R.string.setup_instance_api_disabled)
			binding.instanceCloudflare.visibility = if (isCloudflare) View.VISIBLE else View.GONE
		}
	}

	private fun getFlag(): String {
		val firstLetter = Character.codePointAt(country, 0) - 0x41 + 0x1F1E6
		val secondLetter = Character.codePointAt(country, 1) - 0x41 + 0x1F1E6
		return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
	}
}