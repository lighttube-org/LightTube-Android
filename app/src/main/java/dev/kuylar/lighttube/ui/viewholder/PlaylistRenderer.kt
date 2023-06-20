package dev.kuylar.lighttube.ui.viewholder

import android.app.Activity
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.Utils
import dev.kuylar.lighttube.databinding.RendererPlaylistBinding
import java.text.DecimalFormat

class PlaylistRenderer(val binding: RendererPlaylistBinding) :
	RendererViewHolder(binding.root) {
	override fun bind(item: JsonObject) {
		binding.playlistTitle.text = item.getAsJsonPrimitive("title").asString
		binding.playlistVideoCount.text = binding.root.context.getString(
			R.string.template_videos,
			DecimalFormat().format(item.getAsJsonPrimitive("videoCount").asInt)
		)
		binding.playlistSubtitle.text =
			item.getAsJsonObject("channel").asJsonObject.getAsJsonPrimitive("title").asString

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