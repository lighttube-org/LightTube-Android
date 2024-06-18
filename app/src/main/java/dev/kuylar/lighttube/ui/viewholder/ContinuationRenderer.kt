package dev.kuylar.lighttube.ui.viewholder

import dev.kuylar.lighttube.api.models.UserData
import dev.kuylar.lighttube.api.models.renderers.RendererContainer
import dev.kuylar.lighttube.databinding.RendererContinuationBinding

open class ContinuationRenderer(binding: RendererContinuationBinding) :
	RendererViewHolder(binding.root) {
	override fun bind(renderer: RendererContainer, userData: UserData?) {

	}
}