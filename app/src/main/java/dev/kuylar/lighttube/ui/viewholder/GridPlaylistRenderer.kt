package dev.kuylar.lighttube.ui.viewholder

import android.app.Activity
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.Utils
import dev.kuylar.lighttube.api.models.PlaylistVisibility
import dev.kuylar.lighttube.api.models.UserData
import dev.kuylar.lighttube.databinding.RendererGridPlaylistBinding
import dev.kuylar.lighttube.ui.activity.MainActivity
import java.text.DecimalFormat
import kotlin.concurrent.thread

class GridPlaylistRenderer(val binding: RendererGridPlaylistBinding) :
	RendererViewHolder(binding.root) {
	override fun bind(item: JsonObject, userData: UserData?) {
		binding.playlistTitle.text = item.getAsJsonPrimitive("title").asString
		binding.playlistVideoCount.text = binding.root.context.getString(
			R.string.template_videos,
			DecimalFormat().format(item.getAsJsonPrimitive("videoCount").asInt)
		)

		if (item.getAsJsonPrimitive("id").asString == "!ACTION_NewPlaylist") {
			binding.playlistVideoCount.visibility = View.GONE
			binding.playlistIconContainer.visibility = View.VISIBLE
			with((binding.root.context as MainActivity)) {
				binding.root.setOnClickListener {
					Utils.showPlaylistDialog(
						this,
						layoutInflater,
						getString(R.string.create_playlist),
						getString(R.string.create_playlist_default_title),
						"",
						PlaylistVisibility.Private,
						getString(R.string.action_playlist_create),
						getString(R.string.action_cancel)
					) { dialog, title, description, visibility ->
						thread {
							val createPlaylist =
								getApi().createPlaylist(title, description, visibility)
							runOnUiThread {
								findNavController(R.id.nav_host_fragment_activity_main)
									.navigate(
										R.id.navigation_playlist, bundleOf(
											Pair("id", createPlaylist.data!!.id)
										)
									)
							}
						}
					}
				}
			}
			return
		}

		Glide
			.with(binding.root)
			.load(Utils.getBestImageUrlJson(item.getAsJsonArray("thumbnails")))
			.into(binding.playlistThumbnail)

		binding.root.setOnClickListener {
			(binding.root.context as Activity).findNavController(R.id.nav_host_fragment_activity_main)
				.navigate(
					R.id.navigation_playlist, bundleOf(
						Pair("id", item.getAsJsonPrimitive("id").asString)
					)
				)
		}
	}
}