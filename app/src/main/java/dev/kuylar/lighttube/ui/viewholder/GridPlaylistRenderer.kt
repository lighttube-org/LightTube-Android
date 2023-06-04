package dev.kuylar.lighttube.ui.viewholder

import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.Utils
import dev.kuylar.lighttube.databinding.RendererGridPlaylistBinding
import java.text.DecimalFormat

class GridPlaylistRenderer(val binding: RendererGridPlaylistBinding) :
	RendererViewHolder(binding.root) {
	override fun bind(item: JsonObject) {
		binding.playlistTitle.text = item.getAsJsonPrimitive("title").asString
		binding.playlistVideoCount.text = binding.root.context.getString(
			R.string.template_videos,
			DecimalFormat().format(item.getAsJsonPrimitive("videoCount").asInt)
		)

		Glide
			.with(binding.root)
			.load(Utils.getBestImageUrlJson(item.getAsJsonArray("thumbnails")))
			.into(binding.playlistThumbnail)

		binding.root.setOnClickListener {
			// todo: playlist pages
		}
	}
}