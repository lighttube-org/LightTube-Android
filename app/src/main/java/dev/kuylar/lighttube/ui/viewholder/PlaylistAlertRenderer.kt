package dev.kuylar.lighttube.ui.viewholder

import com.google.gson.JsonObject
import dev.kuylar.lighttube.databinding.RendererPlaylistAlertBinding

class PlaylistAlertRenderer(private val binding: RendererPlaylistAlertBinding) :
	RendererViewHolder(binding.root) {
	override fun bind(item: JsonObject) {
		binding.text.text = item.getAsJsonPrimitive("text").asString
	}
}