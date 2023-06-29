package dev.kuylar.lighttube

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import dev.kuylar.lighttube.api.models.LightTubeImage
import dev.kuylar.lighttube.databinding.RendererChannelBinding
import dev.kuylar.lighttube.databinding.RendererCommentBinding
import dev.kuylar.lighttube.databinding.RendererContinuationBinding
import dev.kuylar.lighttube.databinding.RendererGridPlaylistBinding
import dev.kuylar.lighttube.databinding.RendererPlaylistAlertBinding
import dev.kuylar.lighttube.databinding.RendererPlaylistBinding
import dev.kuylar.lighttube.databinding.RendererPlaylistInfoBinding
import dev.kuylar.lighttube.databinding.RendererPlaylistVideoBinding
import dev.kuylar.lighttube.databinding.RendererSlimVideoInfoBinding
import dev.kuylar.lighttube.databinding.RendererUnknownBinding
import dev.kuylar.lighttube.databinding.RendererVideoBinding
import dev.kuylar.lighttube.ui.viewholder.ChannelRenderer
import dev.kuylar.lighttube.ui.viewholder.CommentRenderer
import dev.kuylar.lighttube.ui.viewholder.ContinuationRenderer
import dev.kuylar.lighttube.ui.viewholder.GridPlaylistRenderer
import dev.kuylar.lighttube.ui.viewholder.PlaylistAlertRenderer
import dev.kuylar.lighttube.ui.viewholder.PlaylistInfoRenderer
import dev.kuylar.lighttube.ui.viewholder.PlaylistRenderer
import dev.kuylar.lighttube.ui.viewholder.PlaylistVideoRenderer
import dev.kuylar.lighttube.ui.viewholder.RendererViewHolder
import dev.kuylar.lighttube.ui.viewholder.SlimVideoInfoRenderer
import dev.kuylar.lighttube.ui.viewholder.UnknownRenderer
import dev.kuylar.lighttube.ui.viewholder.VideoRenderer
import okhttp3.OkHttpClient
import okhttp3.Request
import java.security.MessageDigest


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

		fun getSponsorBlockInfo(videoId: String): SponsorBlockVideo? {
			try {
				val req = Request.Builder().apply {
					val bytes = videoId.toByteArray()
					val md = MessageDigest.getInstance("SHA-256")
					val hash = md.digest(bytes).fold("") { str, it -> str + "%02x".format(it) }
					url(
						"https://sponsor.ajay.app/api/skipSegments/${
							hash.substring(
								0,
								4
							)
						}?category=sponsor&category=selfpromo&category=interaction&category=intro&category=outro&category=preview&category=music_offtopic&category=filler"
					)
					header(
						"User-Agent",
						"LightTube-Android/1.0 (https://github.com/kuylar/lighttube-android)"
					)
				}.build()

				http.newCall(req).execute().use { response ->
					val res = gson.fromJson(
						response.body!!.string(),
						object : TypeToken<List<SponsorBlockVideo>>() {})
					return res.firstOrNull { it.videoId == videoId }
				}
			} catch (e: Exception) {
				return null
			}
		}

		fun checkForUpdates(context: Context): UpdateInfo? {
			var updateInfo: UpdateInfo? = null
			try {
				val req = Request.Builder().apply {
					url("https://api.github.com/repos/kuylar/lighttube-android/releases")
					header(
						"User-Agent",
						"LightTube-Android/1.0 (https://github.com/kuylar/lighttube-android)"
					)
				}.build()

				http.newCall(req).execute().use { response ->
					val res = GsonBuilder()
						.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
						.create()
						.fromJson(
							response.body!!.string(),
							object : TypeToken<List<GithubRelease>>() {})
					val latestVer = res.first().tagName.substring(1)
					val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
					val version = pInfo.versionName.split(" ").first()
					val latestVersionCode = latestVer.replace(".", "").toInt()
					val currentVersionCode = version.replace(".", "").toInt()
					if (latestVersionCode > currentVersionCode) {
						Log.i("UpdateChecker", "Update available! (${version} -> ${latestVer})")
						updateInfo = UpdateInfo(
							version,
							latestVer,
							res.first().assets.first().browserDownloadUrl
						)
					}
				}
			} catch (e: Exception) {
				Log.e("UpdateChecker", e.message, e)
			}
			return updateInfo
		}

		fun getViewHolder(
			renderer: JsonObject,
			inflater: LayoutInflater,
			parent: ViewGroup
		): RendererViewHolder {
			return when (renderer.getAsJsonPrimitive("type").asString) {
				"videoRenderer" -> VideoRenderer(
					RendererVideoBinding.inflate(
						inflater,
						parent,
						false
					)
				)

				"compactVideoRenderer" -> VideoRenderer(
					RendererVideoBinding.inflate(
						inflater,
						parent,
						false
					)
				)

				"channelRenderer" -> ChannelRenderer(
					RendererChannelBinding.inflate(
						inflater,
						parent,
						false
					)
				)

				"commentThreadRenderer" -> CommentRenderer(
					RendererCommentBinding.inflate(
						inflater,
						parent,
						false
					)
				)

				"continuationItemRenderer" -> ContinuationRenderer(
					RendererContinuationBinding.inflate(
						inflater,
						parent,
						false
					)
				)

				"slimVideoInfoRenderer" -> SlimVideoInfoRenderer(
					RendererSlimVideoInfoBinding.inflate(
						inflater,
						parent,
						false
					)
				)

				"gridPlaylistRenderer" -> GridPlaylistRenderer(
					RendererGridPlaylistBinding.inflate(
						inflater,
						parent,
						false
					)
				)

				"playlistRenderer" -> PlaylistRenderer(
					RendererPlaylistBinding.inflate(
						inflater,
						parent,
						false
					)
				)

				"playlistInfoRenderer" -> PlaylistInfoRenderer(
					RendererPlaylistInfoBinding.inflate(
						inflater,
						parent,
						false
					)
				)

				"playlistVideoRenderer" -> PlaylistVideoRenderer(
					RendererPlaylistVideoBinding.inflate(
						inflater,
						parent,
						false
					)
				)

				"playlistAlertRenderer" -> PlaylistAlertRenderer(
					RendererPlaylistAlertBinding.inflate(
						inflater,
						parent,
						false
					)
				)

				// i hate these
				"richItemRenderer" -> getViewHolder(
					renderer.getAsJsonObject("content"),
					inflater,
					parent
				)

				else -> UnknownRenderer(RendererUnknownBinding.inflate(inflater, parent, false))
			}
		}
	}
}