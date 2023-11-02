package dev.kuylar.lighttube

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.google.android.material.button.MaterialButton
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import dev.kuylar.lighttube.api.models.LightTubeImage
import dev.kuylar.lighttube.api.models.SubscriptionInfo
import dev.kuylar.lighttube.databinding.RendererChannelBinding
import dev.kuylar.lighttube.databinding.RendererCommentBinding
import dev.kuylar.lighttube.databinding.RendererContinuationBinding
import dev.kuylar.lighttube.databinding.RendererGridPlaylistBinding
import dev.kuylar.lighttube.databinding.RendererItemSectionBinding
import dev.kuylar.lighttube.databinding.RendererMessageBinding
import dev.kuylar.lighttube.databinding.RendererPlaylistAlertBinding
import dev.kuylar.lighttube.databinding.RendererPlaylistBinding
import dev.kuylar.lighttube.databinding.RendererPlaylistInfoBinding
import dev.kuylar.lighttube.databinding.RendererPlaylistVideoBinding
import dev.kuylar.lighttube.databinding.RendererSlimVideoInfoBinding
import dev.kuylar.lighttube.databinding.RendererUnknownBinding
import dev.kuylar.lighttube.databinding.RendererVideoBinding
import dev.kuylar.lighttube.ui.activity.MainActivity
import dev.kuylar.lighttube.ui.fragment.ManageSubscriptionFragment
import dev.kuylar.lighttube.ui.viewholder.ChannelRenderer
import dev.kuylar.lighttube.ui.viewholder.ChannelVideoPlayerRenderer
import dev.kuylar.lighttube.ui.viewholder.CommentRenderer
import dev.kuylar.lighttube.ui.viewholder.ContinuationRenderer
import dev.kuylar.lighttube.ui.viewholder.GridPlaylistRenderer
import dev.kuylar.lighttube.ui.viewholder.ItemSectionRenderer
import dev.kuylar.lighttube.ui.viewholder.MessageRenderer
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
import kotlin.concurrent.thread


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

		private fun getUserAgent(): String =
			"LightTube-Android/${BuildConfig.VERSION_NAME} (https://github.com/kuylar/lighttube-android)"

		fun getDislikeCount(videoId: String): Long {
			try {
				val req = Request.Builder().apply {
					url("https://returnyoutubedislikeapi.com/votes?videoId=$videoId")
					header("User-Agent", getUserAgent())
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
						}?category=sponsor&category=selfpromo&category=interaction&category=intro&category=outro&category=preview&category=music_offtopic"
					)
					header("User-Agent", getUserAgent())
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

		fun checkForUpdates(): UpdateInfo? {
			var updateInfo: UpdateInfo? = null
			try {
				val req = Request.Builder().apply {
					url("https://api.github.com/repos/kuylar/lighttube-android/releases/latest")
					header("User-Agent", getUserAgent())
				}.build()

				http.newCall(req).execute().use { response ->
					val res = GsonBuilder()
						.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
						.create()
						.fromJson(
							response.body!!.string(),
							GithubRelease::class.java
						)
					val latestVer = res.tagName.substring(1)
					val version = BuildConfig.VERSION_NAME.split(" ").first()
					val latestVersionCode = latestVer.replace(".", "").toInt()
					val currentVersionCode = version.replace(".", "").toInt()
					if (latestVersionCode > currentVersionCode) {
						Log.i("UpdateChecker", "Update available! (${version} -> ${latestVer})")
						updateInfo = UpdateInfo(
							version,
							latestVer,
							res.assets.first().browserDownloadUrl
						)
					}
				}
			} catch (e: Exception) {
				Log.e("UpdateChecker", e.message, e)
			}
			return updateInfo
		}

		fun updateSubscriptionButton(
			context: Context,
			button: MaterialButton,
			subscriptionInfo: SubscriptionInfo
		) {
			if (!subscriptionInfo.subscribed) {
				button.text = context.getString(R.string.subscribe)
				button.setIconResource(0)
				return
			}

			button.text = context.getString(R.string.subscribed)
			if (subscriptionInfo.notifications) {
				button.setIconResource(R.drawable.ic_notifications_on)
			} else {
				button.setIconResource(R.drawable.ic_notifications_off)
			}
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

				"gridChannelRenderer" -> ChannelRenderer(
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

				"channelVideoPlayerRenderer" -> ChannelVideoPlayerRenderer(
					RendererVideoBinding.inflate(
						inflater,
						parent,
						false
					)
				)

				"messageRenderer" -> MessageRenderer(
					RendererMessageBinding.inflate(
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

				"itemSectionRenderer" -> ItemSectionRenderer(
					RendererItemSectionBinding.inflate(
						inflater,
						parent,
						false
					)
				)

				"gridRenderer" -> ItemSectionRenderer(
					RendererItemSectionBinding.inflate(
						inflater,
						parent,
						false
					)
				)

				else -> UnknownRenderer(RendererUnknownBinding.inflate(inflater, parent, false))
			}
		}

		fun subscribe(
			context: Context,
			channelId: String,
			subscriptionInfo: SubscriptionInfo,
			button: MaterialButton,
			after: (SubscriptionInfo) -> Unit
		) {
			if (!subscriptionInfo.subscribed) {
				button.isEnabled = false
				thread {
					with((context as MainActivity)) {
						getApi().subscribe(
							channelId,
							subscribed = true,
							enableNotifications = true
						)
						runOnUiThread {
							after.invoke(SubscriptionInfo(
								subscribed = true,
								notifications = true
							))
							updateSubscriptionButton(
								this,
								button,
								subscriptionInfo
							)
							button.isEnabled = true
						}
					}
				}
			} else {
				val sheet = ManageSubscriptionFragment(channelId, subscriptionInfo) {
					after.invoke(it)
					updateSubscriptionButton(
						context,
						button,
						it
					)
				}
				sheet.show((context as FragmentActivity).supportFragmentManager, null)
			}
		}
	}
}