package dev.kuylar.lighttube.ui.viewholder

import android.app.Activity
import com.google.gson.JsonObject
import dev.kuylar.lighttube.Utils
import dev.kuylar.lighttube.databinding.RendererItemSectionBinding

class ItemSectionRenderer(val binding: RendererItemSectionBinding) :
	RendererViewHolder(binding.root) {
	override fun bind(item: JsonObject) {
		if (!item.get("contents").isJsonNull)
			item.getAsJsonArray("contents").forEach {
				val holder = Utils.getViewHolder(
					it.asJsonObject,
					(binding.root.context as Activity).layoutInflater,
					binding.root
				)
				holder.bind(it.asJsonObject)
				binding.root.addView(holder.itemView)
			}
		else if (!item.get("items").isJsonNull)
			item.getAsJsonArray("items").forEach {
				val holder = Utils.getViewHolder(
					it.asJsonObject,
					(binding.root.context as Activity).layoutInflater,
					binding.root
				)
				holder.bind(it.asJsonObject)
				binding.root.addView(holder.itemView)
			}
	}
}