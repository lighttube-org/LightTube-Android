package dev.kuylar.lighttube.ui.viewholder

import android.view.View
import android.widget.Toast
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.Utils
import dev.kuylar.lighttube.api.models.LightTubePlaylist
import dev.kuylar.lighttube.api.models.PlaylistVisibility
import dev.kuylar.lighttube.api.models.UserData
import dev.kuylar.lighttube.api.models.renderers.RendererContainer
import dev.kuylar.lighttube.api.models.renderers.VideoRendererData
import dev.kuylar.lighttube.databinding.RendererPlaylistInfoBinding
import dev.kuylar.lighttube.ui.activity.MainActivity
import kotlin.concurrent.thread


class PlaylistInfoRenderer(private val binding: RendererPlaylistInfoBinding) :
	RendererViewHolder(binding.root) {
	override fun bind(renderer: RendererContainer, userData: UserData?) {
		val activity = binding.root.context as MainActivity
		val playlist = renderer.data as LightTubePlaylist
		binding.playlistTitle.text = playlist.sidebar.title
		binding.playlistAuthor.text = playlist.sidebar.channel.title
		binding.playlistVideoCount.text = playlist.sidebar.videoCountText
		if (playlist.sidebar.description.isNullOrEmpty())
			binding.playlistDescription.visibility = View.GONE
		else
			binding.playlistDescription.text = playlist.sidebar.description

		Glide
			.with(activity)
			.load(Utils.getBestImageUrl(playlist.sidebar.thumbnails))
			.into(binding.playlistThumbnail)

		val firstVideo = playlist.contents.firstOrNull {it.type == "video"}?.data as VideoRendererData
		binding.buttonPlayAll.setOnClickListener {
			activity.getPlayer().playVideo(firstVideo.videoId)
		}

		binding.buttonShuffle.setOnClickListener {
			activity.getPlayer().playVideo(firstVideo.videoId)
		}

		if (playlist.editable) {
			(binding.root.context as MainActivity).apply {
				binding.buttonEditPlaylist.setOnClickListener {
					Utils.showPlaylistDialog(
						this,
						layoutInflater,
						getString(R.string.edit_playlist_title),
						playlist.sidebar.title,
						playlist.sidebar.description ?: "",
						PlaylistVisibility.Private,
						getString(R.string.edit_playlist_submit),
						getString(R.string.edit_playlist_cancel),
					) { dialog, title, description, visibility ->
						thread {
							getApi().updatePlaylist(
								playlist.id,
								title,
								description,
								visibility
							)
							runOnUiThread {
								dialog.dismiss()
							}
						}
					}
				}

				binding.buttonDeletePlaylist.setOnClickListener {
					MaterialAlertDialogBuilder(binding.root.context)
						.setTitle(getString(R.string.delete_playlist_title, playlist.sidebar.title))
						.setMessage(R.string.delete_playlist_body)
						.setPositiveButton(R.string.delete_playlist) { dialog, _ ->
							thread {
								getApi().deletePlaylist(playlist.id)
								runOnUiThread {
									Toast.makeText(
										this,
										R.string.delete_playlist_success,
										Toast.LENGTH_LONG
									).show()
									findNavController(R.id.nav_host_fragment_activity_main).navigateUp()
									dialog.dismiss()
								}
							}
						}
						.setNegativeButton(R.string.delete_playlist_cancel) { dialog, _ ->
							dialog.dismiss()
						}
						.show()
				}
			}
		} else {
			binding.buttonEditPlaylist.visibility = View.GONE
			binding.buttonDeletePlaylist.visibility = View.GONE
		}
	}
}