package dev.kuylar.lighttube.ui.viewholder

import dev.kuylar.lighttube.api.models.UserData
import dev.kuylar.lighttube.api.models.renderers.MessageRendererData
import dev.kuylar.lighttube.api.models.renderers.RendererContainer
import dev.kuylar.lighttube.databinding.RendererMessageBinding

class MessageRenderer(private val binding: RendererMessageBinding) :
	RendererViewHolder(binding.root) {
	override fun bind(renderer: RendererContainer, userData: UserData?) {
		binding.root.text = (renderer.data as MessageRendererData).message
	}
}