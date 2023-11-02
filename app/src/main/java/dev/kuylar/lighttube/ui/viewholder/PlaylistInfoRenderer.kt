package dev.kuylar.lighttube.ui.viewholder

import android.view.View
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.JsonObject
import dev.kuylar.lighttube.Utils
import dev.kuylar.lighttube.api.models.LightTubePlaylist
import dev.kuylar.lighttube.api.models.UserData
import dev.kuylar.lighttube.databinding.RendererPlaylistInfoBinding
import dev.kuylar.lighttube.ui.activity.MainActivity

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
	}
}