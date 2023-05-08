package dev.kuylar.lighttube.ui.viewholder

import com.google.gson.JsonObject
import dev.kuylar.lighttube.databinding.RendererUnknownBinding

class UnknownRenderer(val binding: RendererUnknownBinding) : RendererViewHolder(binding.root) {
	override fun bind(item: JsonObject) {
		binding.rendererSubtitle.text = item.getAsJsonPrimitive("type").asString
	}
}