package dev.kuylar.lighttube.ui.viewholder

import com.google.gson.JsonObject
import dev.kuylar.lighttube.api.models.UserData
import dev.kuylar.lighttube.databinding.RendererContinuationBinding

open class ContinuationRenderer(binding: RendererContinuationBinding) :
	RendererViewHolder(binding.root) {
	override fun bind(item: JsonObject, userData: UserData?) {

	}
}