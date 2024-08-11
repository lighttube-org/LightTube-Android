package dev.kuylar.lighttube.ui.viewholder

import android.app.Activity
import android.content.res.Configuration
import dev.kuylar.lighttube.Utils
import dev.kuylar.lighttube.api.models.UserData
import dev.kuylar.lighttube.api.models.renderers.ContainerRendererData
import dev.kuylar.lighttube.api.models.renderers.RendererContainer
import dev.kuylar.lighttube.databinding.RendererItemSectionBinding

class ItemSectionRenderer(val binding: RendererItemSectionBinding) :
	RendererViewHolder(binding.root) {
	override fun bind(renderer: RendererContainer, userData: UserData?) {
		val item = renderer.data as ContainerRendererData
		binding.root.removeAllViews()
		if (item.items.isNotEmpty()) {
			item.items.forEach {
				val holder = Utils.getViewHolder(
					it,
					(binding.root.context as Activity).layoutInflater,
					binding.root,
					binding.root.context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
				)
				holder.bind(it, userData)
				binding.root.addView(holder.itemView)
			}
		}
	}
}