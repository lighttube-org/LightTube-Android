package dev.kuylar.lighttube.ui.viewholder

import dev.kuylar.lighttube.api.models.UserData
import dev.kuylar.lighttube.api.models.renderers.MessageRendererData
import dev.kuylar.lighttube.api.models.renderers.RendererContainer
import dev.kuylar.lighttube.databinding.RendererPlaylistAlertBinding

class PlaylistAlertRenderer(private val binding: RendererPlaylistAlertBinding) :
	RendererViewHolder(binding.root) {
	override fun bind(renderer: RendererContainer, userData: UserData?) {
		binding.text.text = (renderer.data as MessageRendererData).message
	}
}