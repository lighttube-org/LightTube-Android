package dev.kuylar.lighttube.api

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.kuylar.lighttube.api.models.ApiResponse
import dev.kuylar.lighttube.api.models.LightTubeUserInfo
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder


class LightTubeApi(context: Context) {
	private val tag = "LightTubeApi"
	private val client = OkHttpClient()
	private val gson = Gson()

	private val host: String
	private val refreshToken: String?

	init {
		val sp = context.getSharedPreferences("main", Context.MODE_PRIVATE)
		host = sp.getString("instanceHost", "")!!
		refreshToken = sp.getString("refreshToken", null)
		Log.i(
			tag,
			"Initialized the API for $host ${if (refreshToken != null) "with" else "without"} a refresh token"
		)
	}

	private fun <T> get(token: TypeToken<ApiResponse<T>>, path: String, query: HashMap<String, String> = HashMap()): ApiResponse<T> {
		val request: Request = Request.Builder().apply {
			url("$host/api/$path${query.toUrl()}")
			if (refreshToken != null) {
				header("Authorization", "Bearer ${UtilityApi.getToken(host, refreshToken)}")
			}
		}.build()

		client.newCall(request).execute().use { response ->
			return Gson().fromJson(
				response.body!!.string(),
				token.type
			)
		}
	}

	fun getCurrentUser(): ApiResponse<LightTubeUserInfo> {
		return get<LightTubeUserInfo>(object : TypeToken<ApiResponse<LightTubeUserInfo>>() {}, "currentUser")
	}
}

private fun <K, V> HashMap<K, V>.toUrl(): String {
	var res = "?"
	forEach {
		res += "${
			URLEncoder.encode(
				it.key as String,
				"utf8"
			)
		}=${URLEncoder.encode(it.value as String, "utf8")}"
	}
	return res
}
