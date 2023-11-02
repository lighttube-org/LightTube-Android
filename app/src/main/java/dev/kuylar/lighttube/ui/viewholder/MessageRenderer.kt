package dev.kuylar.lighttube.ui.viewholder

import com.google.gson.JsonObject
import dev.kuylar.lighttube.api.models.UserData
import dev.kuylar.lighttube.databinding.RendererMessageBinding

class MessageRenderer(private val binding: RendererMessageBinding) :
	RendererViewHolder(binding.root) {
	override fun bind(item: JsonObject, userData: UserData?) {
		binding.root.text = item.getAsJsonPrimitive("message").asString
	}
}