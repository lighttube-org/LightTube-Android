package dev.kuylar.lighttube.ui.viewholder

import com.google.gson.JsonObject
import dev.kuylar.lighttube.api.models.UserData
import dev.kuylar.lighttube.databinding.RendererUnknownBinding

class UnknownRenderer(val binding: RendererUnknownBinding) : RendererViewHolder(binding.root) {
	override fun bind(item: JsonObject, userData: UserData?) {
		binding.rendererSubtitle.text = item.getAsJsonPrimitive("type").asString
	}
}