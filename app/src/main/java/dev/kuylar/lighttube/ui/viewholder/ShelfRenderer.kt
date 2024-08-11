package dev.kuylar.lighttube.ui.viewholder

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.api.models.UserData
import dev.kuylar.lighttube.api.models.renderers.ContainerRendererData
import dev.kuylar.lighttube.api.models.renderers.RendererContainer
import dev.kuylar.lighttube.databinding.RendererShelfBinding
import dev.kuylar.lighttube.ui.adapter.RendererRecyclerAdapter

class ShelfRenderer(private val binding: RendererShelfBinding) :
	RendererViewHolder(binding.root) {
	override fun bind(renderer: RendererContainer, userData: UserData?) {
		val item = renderer.data as ContainerRendererData

		binding.shelfTitle.text = item.title
		if (!item.subtitle.isNullOrEmpty())
			binding.shelfSubtitle.text = item.subtitle
		else
			binding.shelfSubtitle.visibility = View.GONE

		val direction = if (item.style.contains(";")) item.style.split(";")[1] else "vertical"

		val adapter = RendererRecyclerAdapter(item.items.toMutableList(), null)
		adapter.updateUserData(userData)
		binding.recyclerShelf.adapter = adapter

		val context = binding.root.context
		when (direction) {
			"horizontal" -> {
				binding.recyclerShelf.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
				binding.shelfExpandButton.visibility = View.GONE
				adapter.setFlag("forcePortrait")
				adapter.setFlag("smaller")
			}
			else -> {
				binding.recyclerShelf.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
				binding.recyclerShelf.isNestedScrollingEnabled = true
				if (item.items.size > 3) {
					binding.shelfExpandButton.text = context.getString(R.string.template_shelf_expand, item.items.size - 3)
					binding.shelfExpandButton.setOnClickListener {
						if (adapter.isExpanded) {
							binding.shelfExpandButton.text = context.getString(R.string.template_shelf_expand, item.items.size - 3)
							adapter.setExpanded(false)
						} else {
							binding.shelfExpandButton.text = context.getString(R.string.show_less)
							adapter.setExpanded(true)
						}
					}
					adapter.setExpandable(3)
				} else {
					binding.shelfExpandButton.visibility = View.GONE
				}
			}
		}
	}
}