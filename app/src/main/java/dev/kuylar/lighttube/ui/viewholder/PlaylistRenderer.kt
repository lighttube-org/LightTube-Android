package dev.kuylar.lighttube.ui.viewholder

import android.app.Activity
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.Utils
import dev.kuylar.lighttube.api.models.PlaylistVisibility
import dev.kuylar.lighttube.api.models.UserData
import dev.kuylar.lighttube.api.models.renderers.PlaylistRendererData
import dev.kuylar.lighttube.api.models.renderers.RendererContainer
import dev.kuylar.lighttube.databinding.RendererPlaylistBinding
import dev.kuylar.lighttube.ui.activity.MainActivity
import java.text.DecimalFormat
import kotlin.concurrent.thread

class PlaylistRenderer(val binding: RendererPlaylistBinding) :
	RendererViewHolder(binding.root) {
	override fun bind(renderer: RendererContainer, userData: UserData?) {
		val item = renderer.data as PlaylistRendererData

		binding.playlistTitle.text = item.title
		binding.playlistVideoCount.text = binding.root.context.resources.getQuantityString(
			R.plurals.template_videos,
			item.videoCount.toInt(),
			DecimalFormat().format(item.videoCount)
		)

		if (item.playlistId == "!ACTION_NewPlaylist") {
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
					) { _, title, description, visibility ->
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

		if (item.author?.title != null)
			binding.playlistSubtitle.text = item.author.title
		else
			binding.playlistSubtitle.visibility = View.GONE

		Glide
			.with(binding.root)
			.load(Utils.getBestImageUrl(item.thumbnails))
			.into(binding.playlistThumbnail)

		binding.root.setOnClickListener {
			(binding.root.context as Activity).findNavController(R.id.nav_host_fragment_activity_main)
				.navigate(
					R.id.navigation_playlist, bundleOf(
						Pair("id", item.playlistId)
					)
				)
		}
	}
}