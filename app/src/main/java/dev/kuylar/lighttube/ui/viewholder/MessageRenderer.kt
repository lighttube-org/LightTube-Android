package dev.kuylar.lighttube.ui.viewholder

import com.google.gson.JsonObject
import dev.kuylar.lighttube.databinding.RendererMessageBinding

class MessageRenderer(private val binding: RendererMessageBinding) :
	RendererViewHolder(binding.root) {
	override fun bind(item: JsonObject) {
		binding.root.text = item.getAsJsonPrimitive("message").asString
	}
}