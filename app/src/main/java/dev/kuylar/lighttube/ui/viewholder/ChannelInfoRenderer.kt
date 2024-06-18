package dev.kuylar.lighttube.ui.viewholder

import dev.kuylar.lighttube.api.models.LightTubeChannel
import dev.kuylar.lighttube.api.models.UserData
import dev.kuylar.lighttube.api.models.renderers.RendererContainer
import dev.kuylar.lighttube.databinding.RendererChannelInfoBinding

class ChannelInfoRenderer(private val binding: RendererChannelInfoBinding) :
	RendererViewHolder(binding.root) {
	override fun bind(renderer: RendererContainer, userData: UserData?) {
		val channel = renderer as LightTubeChannel
		channel.fillBinding(binding.header, userData)
	}
}