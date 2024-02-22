package dev.kuylar.lighttube

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import dev.kuylar.lighttube.api.models.LightTubeImage
import dev.kuylar.lighttube.api.models.PlaylistVisibility
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
import dev.kuylar.lighttube.databinding.RendererVideoLandscapeBinding
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
			parent: ViewGroup,
			landscape: Boolean
		): RendererViewHolder {
			return when (renderer.getAsJsonPrimitive("type").asString) {
				"videoRenderer" -> VideoRenderer(
					if (landscape) RendererVideoBinding.bind(
						RendererVideoLandscapeBinding.inflate(
							inflater,
							parent,
							false
						).root
					) else
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
					if (landscape) RendererVideoBinding.bind(
						RendererVideoLandscapeBinding.inflate(
							inflater,
							parent,
							false
						).root
					) else
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
					parent,
					landscape
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
							after.invoke(
								SubscriptionInfo(
									subscribed = true,
									notifications = true
								)
							)
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

		fun parsePlaylistVisibility(text: String, resources: Resources): PlaylistVisibility {
			val arr = resources.getStringArray(R.array.playlist_visibility)
			val visibleText = arr[2]
			val unlistedText = arr[1]
			val privateText = arr[0]
			return when (text) {
				visibleText -> PlaylistVisibility.Visible
				unlistedText -> PlaylistVisibility.Unlisted
				privateText -> PlaylistVisibility.Private
				else -> PlaylistVisibility.Private
			}
		}

		fun getPlaylistVisibility(visibility: PlaylistVisibility, resources: Resources): String {
			val arr = resources.getStringArray(R.array.playlist_visibility)
			val visibleText = arr[2]
			val unlistedText = arr[1]
			val privateText = arr[0]
			return when (visibility) {
				PlaylistVisibility.Visible -> visibleText
				PlaylistVisibility.Unlisted -> unlistedText
				PlaylistVisibility.Private -> privateText
			}
		}

		fun showPlaylistDialog(
			context: Context,
			layoutInflater: LayoutInflater,
			header: String,
			title: String,
			description: String,
			visibility: PlaylistVisibility,
			positiveButtonText: String,
			negativeButtonText: String,
			positiveAction: (dialog: DialogInterface, title: String, description: String, visibility: PlaylistVisibility) -> Unit
		) {
			val v = layoutInflater.inflate(R.layout.dialog_edit_playlist, null)
			val titleView = v.findViewById<TextInputLayout>(R.id.playlist_title)
			val descriptionView =
				v.findViewById<TextInputLayout>(R.id.playlist_description)
			val visibilityView =
				v.findViewById<TextInputLayout>(R.id.playlist_visibility)

			titleView.editText!!.setText(title)
			descriptionView.editText!!.setText(description)
			(visibilityView.editText!! as MaterialAutoCompleteTextView).setText(
				getPlaylistVisibility(
					visibility,
					context.resources
				), false
			)

			MaterialAlertDialogBuilder(context)
				.setTitle(header)
				.setView(v)
				.setPositiveButton(positiveButtonText) { dialog, _ ->
					positiveAction(
						dialog, titleView.editText!!.editableText.toString(),
						descriptionView.editText!!.editableText.toString(),
						parsePlaylistVisibility(
							visibilityView.editText!!.editableText.toString(),
							context.resources
						)
					)
				}
				.setNegativeButton(negativeButtonText) { dialog, _ ->
					dialog.dismiss()
				}
				.show()
		}

		fun parseQueryString(query: String): HashMap<String, String> {
			val queryString = query.trimStart('?')
			val dict = HashMap<String, String>()
			queryString.split('&').forEach {
				val parts = it.split('=')
				val name = parts.firstOrNull() ?: ""
				val value = parts.drop(1).joinToString("=")
				dict[name] = value
			}
			return dict
		}

		fun unwrapAttributionUrl(query: String): String {
			return parseQueryString(query)["u"] ?: ""
		}

		@SuppressLint("NotifyDataSetChanged")
		fun rebindAllRecyclerViews(recycler: RecyclerView) {
			val adapter = recycler.adapter
			val layoutManager = recycler.layoutManager
			val state = layoutManager?.onSaveInstanceState()
			recycler.adapter = adapter
			recycler.layoutManager = layoutManager
			recycler.adapter?.notifyDataSetChanged()
			layoutManager?.onRestoreInstanceState(state)
		}
	}
}