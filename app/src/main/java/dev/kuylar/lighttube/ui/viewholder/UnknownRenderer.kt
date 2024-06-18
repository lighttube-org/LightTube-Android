package dev.kuylar.lighttube.ui.viewholder

import android.annotation.SuppressLint
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.api.models.UserData
import dev.kuylar.lighttube.api.models.renderers.ExceptionRendererData
import dev.kuylar.lighttube.api.models.renderers.RendererContainer
import dev.kuylar.lighttube.databinding.RendererUnknownBinding

class UnknownRenderer(val binding: RendererUnknownBinding) : RendererViewHolder(binding.root) {
	@SuppressLint("SetTextI18n")
	override fun bind(renderer: RendererContainer, userData: UserData?) {
		if (renderer.type == "exception") {
			val ex = renderer.data as ExceptionRendererData
			binding.rendererTitle.text =
				binding.root.context.getString(R.string.renderer_exception, ex.rendererCase)
			binding.rendererSubtitle.text = ex.message
		} else {
			binding.rendererSubtitle.text = renderer.type
		}
	}
}