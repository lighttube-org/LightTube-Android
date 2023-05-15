package dev.kuylar.lighttube.api

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import dev.kuylar.lighttube.api.models.ApiResponse
import dev.kuylar.lighttube.api.models.ContinuationContainer
import dev.kuylar.lighttube.api.models.InstanceInfo
import dev.kuylar.lighttube.api.models.LightTubeException
import dev.kuylar.lighttube.api.models.LightTubePlayer
import dev.kuylar.lighttube.api.models.LightTubeUserInfo
import dev.kuylar.lighttube.api.models.LightTubeVideo
import dev.kuylar.lighttube.api.models.SearchResults
import dev.kuylar.lighttube.api.models.SearchSuggestions
import dev.kuylar.lighttube.api.models.SubscriptionFeedItem
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.URLEncoder


class LightTubeApi(context: Context) {
	private val tag = "LightTubeApi"
	private val client = OkHttpClient()
	private val gson = GsonBuilder().apply {
		setDateFormat("yyyy-MM-dd'T'HH:mm:ssX")
	}.create()

	val host: String
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

	@Throws(LightTubeException::class, IOException::class)
	private fun <T> get(
		token: TypeToken<ApiResponse<T>>,
		path: String,
		query: HashMap<String, String> = HashMap()
	): ApiResponse<T> {
		val request: Request = Request.Builder().apply {
			url("$host/api/$path${query.toUrl()}")
			if (refreshToken != null) {
				header("Authorization", "Bearer ${UtilityApi.getToken(host, refreshToken)}")
			}
		}.build()

		client.newCall(request).execute().use { response ->
			if (!response.headers["Content-Type"]?.contains("json")!!)
				throw LightTubeException(0, "Received non-JSON response")
			val r = gson.fromJson<ApiResponse<T>>(
				response.body!!.string(),
				token.type
			)
			if (r.error != null)
				throw r.error
			if (r.data == null)
				throw LightTubeException(0, "Received null date")
			return r
		}
	}

	@Throws(LightTubeException::class, IOException::class)
	fun getCurrentUser(): ApiResponse<LightTubeUserInfo> {
		return get(
			object : TypeToken<ApiResponse<LightTubeUserInfo>>() {},
			"currentUser"
		)
	}

	@Throws(LightTubeException::class, IOException::class, Exception::class)
	fun getInstanceInfo(): InstanceInfo {
		val request: Request = Request.Builder()
			.url("$host/api/info")
			.build()

		client.newCall(request).execute().use { response ->
			if (response.code != 200)
				throw Exception("HTTP ${response.code} while trying to get instance info")
			return gson.fromJson(
				response.body!!.string(),
				InstanceInfo::class.java
			)
		}
	}

	@Throws(LightTubeException::class, IOException::class)
	fun getPlayer(id: String): ApiResponse<LightTubePlayer> {
		return get(
			object : TypeToken<ApiResponse<LightTubePlayer>>() {},
			"player",
			hashMapOf(Pair("id", id))
		)
	}

	@Throws(LightTubeException::class, IOException::class)
	fun getVideo(id: String, playlistId: String?): ApiResponse<LightTubeVideo> {
		val data = hashMapOf(Pair("id", id))
		if (playlistId != null)
			data["playlistId"] = playlistId
		return get(
			object : TypeToken<ApiResponse<LightTubeVideo>>() {},
			"video",
			data
		)
	}

	@Throws(LightTubeException::class, IOException::class)
	fun search(query: String, params: String? = null): ApiResponse<SearchResults> {
		val data = hashMapOf(Pair("query", query))
		if (params != null)
			data["params"] = params
		return get(
			object : TypeToken<ApiResponse<SearchResults>>() {},
			"search",
			data
		)
	}

	@Throws(LightTubeException::class, IOException::class)
	fun continueSearch(continuation: String): ApiResponse<SearchResults> {
		return get(
			object : TypeToken<ApiResponse<SearchResults>>() {},
			"search",
			hashMapOf(Pair("continuation", continuation))
		)
	}

	@Throws(LightTubeException::class, IOException::class)
	fun searchSuggestions(query: String): ApiResponse<SearchSuggestions> {
		return get(
			object : TypeToken<ApiResponse<SearchSuggestions>>() {},
			"searchSuggestions",
			hashMapOf(Pair("query", query))
		)
	}

	fun getComments(continuation: String): ApiResponse<ContinuationContainer<JsonObject>> {
		return get(
			object : TypeToken<ApiResponse<ContinuationContainer<JsonObject>>>() {},
			"comments",
			hashMapOf(Pair("continuation", continuation))
		)
	}

	@Throws(LightTubeException::class, IOException::class)
	fun getSubscriptionFeed(
		skip: Int = 0,
		limit: Int = 50
	): ApiResponse<List<SubscriptionFeedItem>> {
		return get(
			object : TypeToken<ApiResponse<List<SubscriptionFeedItem>>>() {},
			"feed",
			hashMapOf(Pair("skip", skip.toString()), Pair("limit", limit.toString()))
		)
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
		}=${URLEncoder.encode(it.value as String, "utf8")}&"
	}
	return res.trimEnd('&')
}
