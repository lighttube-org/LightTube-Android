package dev.kuylar.lighttube.ui.viewholder

import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.JsonObject
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.Utils
import dev.kuylar.lighttube.api.models.LightTubePlaylist
import dev.kuylar.lighttube.api.models.PlaylistVisibility
import dev.kuylar.lighttube.api.models.UserData
import dev.kuylar.lighttube.databinding.RendererPlaylistInfoBinding
import dev.kuylar.lighttube.ui.activity.MainActivity
import kotlin.concurrent.thread


class PlaylistInfoRenderer(private val binding: RendererPlaylistInfoBinding) :
	RendererViewHolder(binding.root) {
	override fun bind(item: JsonObject, userData: UserData?) {
		val activity = binding.root.context as MainActivity
		val playlist = Gson().fromJson(item, LightTubePlaylist::class.java)
		binding.playlistTitle.text = playlist.title
		binding.playlistAuthor.text = playlist.channel.title
		binding.playlistVideoCount.text = playlist.videoCountText
		if (playlist.description.isNullOrEmpty())
			binding.playlistDescription.visibility = View.GONE
		else
			binding.playlistDescription.text = playlist.description

		Glide
			.with(activity)
			.load(Utils.getBestImageUrl(playlist.thumbnails))
			.into(binding.playlistThumbnail)

		binding.buttonPlayAll.setOnClickListener {
			activity.player.playVideo(playlist.videos.first().asJsonObject.getAsJsonPrimitive("id").asString)
		}

		binding.buttonShuffle.setOnClickListener {
			activity.player.playVideo(playlist.videos.first().asJsonObject.getAsJsonPrimitive("id").asString)
		}

		if (playlist.editable) {
			(binding.root.context as MainActivity).apply {
				binding.buttonEditPlaylist.setOnClickListener {
					Utils.showPlaylistDialog(
						this,
						layoutInflater,
						getString(R.string.edit_playlist_title),
						playlist.title,
						playlist.description ?: "",
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
						.setTitle(getString(R.string.delete_playlist_title, playlist.title))
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
		}
	}
}