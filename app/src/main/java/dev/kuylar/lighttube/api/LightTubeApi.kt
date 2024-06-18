package dev.kuylar.lighttube.api

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import dev.kuylar.lighttube.api.models.ApiResponse
import dev.kuylar.lighttube.api.models.ContinuationContainer
import dev.kuylar.lighttube.api.models.InstanceInfo
import dev.kuylar.lighttube.api.models.LightTubeChannel
import dev.kuylar.lighttube.api.models.LightTubeException
import dev.kuylar.lighttube.api.models.LightTubePlayer
import dev.kuylar.lighttube.api.models.LightTubePlaylist
import dev.kuylar.lighttube.api.models.LightTubeUserInfo
import dev.kuylar.lighttube.api.models.LightTubeVideo
import dev.kuylar.lighttube.api.models.ModifyPlaylistContentResponse
import dev.kuylar.lighttube.api.models.PlaylistVisibility
import dev.kuylar.lighttube.api.models.SearchResults
import dev.kuylar.lighttube.api.models.SearchSuggestions
import dev.kuylar.lighttube.api.models.SortOrder
import dev.kuylar.lighttube.api.models.SubscriptionChannel
import dev.kuylar.lighttube.api.models.UpdateSubscriptionsResponse
import dev.kuylar.lighttube.api.models.UserPlaylist
import dev.kuylar.lighttube.api.models.renderers.RendererContainer
import dev.kuylar.lighttube.api.models.renderers.RendererSerializer
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.EMPTY_REQUEST
import java.io.IOException
import java.net.URLEncoder


class LightTubeApi {
	private val tag = "LightTubeApi"
	private val client = OkHttpClient()
	private val gson = GsonBuilder().apply {
		setDateFormat("yyyy-MM-dd'T'HH:mm:ssX")
		registerTypeAdapter(RendererContainer::class.java, RendererSerializer())
	}.create()
	var currentUser: LightTubeUserInfo? = null

	val host: String
	private val refreshToken: String?

	constructor(context: Context, refreshToken: String? = null) {
		val sp = context.getSharedPreferences("main", Context.MODE_PRIVATE)
		host = sp.getString("instanceHost", "")!!
		this.refreshToken = refreshToken ?: sp.getString("refreshToken", null)
		Log.i(
			tag,
			"Initialized the API for $host ${if (refreshToken != null) "with" else "without"} a refresh token"
		)
	}

	constructor(instance: String) {
		host = instance
		refreshToken = null
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
	private fun <T> post(
		token: TypeToken<ApiResponse<T>>,
		method: String,
		path: String,
		query: HashMap<String, String> = HashMap(),
		body: RequestBody
	): ApiResponse<T> {
		val request: Request = Request.Builder().apply {
			url("$host/api/$path${query.toUrl()}")
			method(method, body)
			if (refreshToken != null) {
				header("Authorization", "Bearer ${UtilityApi.getToken(host, refreshToken)}")
			}
		}.build()

		client.newCall(request).execute().use { response ->
			if (!response.headers["Content-Type"]?.contains("json")!!)
				throw LightTubeException(0, "Received non-JSON response")
			val json = response.body!!.string()
			val r = gson.fromJson<ApiResponse<T>>(
				json,
				token.type
			)
			if (r.error != null)
				throw r.error
			if (r.data == null)
				throw LightTubeException(0, "Received null date")
			return r
		}
	}

	private fun jsonBody(data: Any?) =
		gson.toJson(data).toRequestBody("application/json".toMediaType())

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
	fun continueRecommendations(continuation: String): ApiResponse<ContinuationContainer<RendererContainer>> {
		return get(
			object : TypeToken<ApiResponse<ContinuationContainer<RendererContainer>>>() {},
			"comments",
			hashMapOf(Pair("continuation", continuation))
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

	fun getComments(
		videoId: String,
		sortBy: SortOrder
	): ApiResponse<ContinuationContainer<RendererContainer>> {
		return get(
			object : TypeToken<ApiResponse<ContinuationContainer<RendererContainer>>>() {},
			"comments",
			hashMapOf(Pair("id", videoId), Pair("sortBy", sortBy.toString()))
		)
	}

	fun continueComments(continuation: String): ApiResponse<ContinuationContainer<RendererContainer>> {
		return get(
			object : TypeToken<ApiResponse<ContinuationContainer<RendererContainer>>>() {},
			"comments",
			hashMapOf(Pair("continuation", continuation))
		)
	}

	@Throws(LightTubeException::class, IOException::class)
	fun getPlaylist(id: String): ApiResponse<LightTubePlaylist> {
		val res = get(
			object : TypeToken<ApiResponse<LightTubePlaylist>>() {},
			"playlist",
			hashMapOf(Pair("id", id))
		)
		if (res.userData != null) {
			res.userData.editable = res.userData.user?.ltChannelID == res.data?.sidebar?.channel?.id
			res.userData.playlistId = res.data?.id
		}
		return res
	}

	@Throws(LightTubeException::class, IOException::class)
	fun continuePlaylist(contKey: String): ApiResponse<LightTubePlaylist> {
		return get(
			object : TypeToken<ApiResponse<LightTubePlaylist>>() {},
			"playlist",
			hashMapOf(Pair("continuation", contKey))
		)
	}

	fun getChannel(id: String, tab: String = "home"): ApiResponse<LightTubeChannel> {
		return get(
			object : TypeToken<ApiResponse<LightTubeChannel>>() {},
			"channel",
			hashMapOf(Pair("id", id), Pair("tab", tab))
		)
	}

	fun continueChannel(contKey: String): ApiResponse<LightTubeChannel> {
		return get(
			object : TypeToken<ApiResponse<LightTubeChannel>>() {},
			"channel",
			hashMapOf(Pair("continuation", contKey))
		)
	}

	// ======= OAUTH RELATED STUFF =======
	@Throws(LightTubeException::class, IOException::class)
	fun getCurrentUser(): ApiResponse<LightTubeUserInfo> {
		return get(
			object : TypeToken<ApiResponse<LightTubeUserInfo>>() {},
			"currentUser"
		)
	}

	fun getSubscriptions(channel: String? = null): ApiResponse<Map<String, SubscriptionChannel>> {
		return get(
			object : TypeToken<ApiResponse<Map<String, SubscriptionChannel>>>() {},
			"subscriptions",
			if (channel != null) hashMapOf(Pair("channel", channel.toString())) else hashMapOf()
		)
	}

	fun subscribe(
		channelId: String,
		subscribed: Boolean,
		enableNotifications: Boolean
	): ApiResponse<UpdateSubscriptionsResponse> {
		return post(
			object : TypeToken<ApiResponse<UpdateSubscriptionsResponse>>() {},
			"PUT",
			"subscriptions",
			hashMapOf(),
			jsonBody(
				mapOf(
					Pair("channelId", channelId),
					Pair("subscribed", subscribed),
					Pair("enableNotifications", enableNotifications)
				)
			)
		)
	}

	@Throws(LightTubeException::class, IOException::class)
	fun getSubscriptionFeed(
		skip: Int = 0,
		limit: Int = 50
	): ApiResponse<List<RendererContainer>> {
		return get(
			object : TypeToken<ApiResponse<List<RendererContainer>>>() {},
			"feed",
			hashMapOf(Pair("skip", skip.toString()), Pair("limit", limit.toString()))
		)
	}

	@Throws(LightTubeException::class, IOException::class)
	fun getLibraryPlaylists(): ApiResponse<List<RendererContainer>> {
		return get(
			object : TypeToken<ApiResponse<List<RendererContainer>>>() {},
			"playlists"
		)
	}

	fun createPlaylist(
		title: String,
		description: String?,
		visibility: PlaylistVisibility
	): ApiResponse<UserPlaylist> {
		return post(
			object : TypeToken<ApiResponse<UserPlaylist>>() {},
			"PUT",
			"playlists",
			hashMapOf(),
			jsonBody(
				mapOf(
					Pair("title", title),
					Pair("description", description),
					Pair("visibility", visibility)
				)
			)
		)
	}

	fun updatePlaylist(
		id: String,
		title: String,
		description: String?,
		visibility: PlaylistVisibility
	): ApiResponse<UserPlaylist> {
		return post(
			object : TypeToken<ApiResponse<UserPlaylist>>() {},
			"PATCH",
			"playlists/$id",
			hashMapOf(),
			jsonBody(
				mapOf(
					Pair("title", title),
					Pair("description", description),
					Pair("visibility", visibility)
				)
			)
		)
	}

	fun deletePlaylist(
		id: String
	): ApiResponse<String> {
		return post(
			object : TypeToken<ApiResponse<String>>() {},
			"DELETE",
			"playlists/$id",
			hashMapOf(),
			EMPTY_REQUEST
		)
	}

	fun addVideoToPlaylist(
		playlistId: String,
		videoId: String
	): ApiResponse<ModifyPlaylistContentResponse> {
		return post(
			object : TypeToken<ApiResponse<ModifyPlaylistContentResponse>>() {},
			"PUT",
			"playlists/$playlistId/$videoId",
			hashMapOf(),
			EMPTY_REQUEST
		)
	}

	fun deleteVideoFromPlaylist(
		playlistId: String,
		videoId: String
	): ApiResponse<String> {
		return post(
			object : TypeToken<ApiResponse<String>>() {},
			"DELETE",
			"playlists/$playlistId/$videoId",
			hashMapOf(),
			EMPTY_REQUEST
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
