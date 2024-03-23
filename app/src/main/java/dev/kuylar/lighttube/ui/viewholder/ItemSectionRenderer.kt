package dev.kuylar.lighttube.ui.viewholder

import android.app.Activity
import android.content.res.Configuration
import com.google.gson.JsonObject
import dev.kuylar.lighttube.Utils
import dev.kuylar.lighttube.api.models.UserData
import dev.kuylar.lighttube.databinding.RendererItemSectionBinding

class ItemSectionRenderer(val binding: RendererItemSectionBinding) :
	RendererViewHolder(binding.root) {
	override fun bind(item: JsonObject, userData: UserData?) {
		if (item.get("contents") != null)
			item.getAsJsonArray("contents").forEach {
				val holder = Utils.getViewHolder(
					it.asJsonObject,
					(binding.root.context as Activity).layoutInflater,
					binding.root,
					binding.root.context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
				)
				holder.bind(it.asJsonObject, userData)
				binding.root.addView(holder.itemView)
			}
		else if (item.get("items") != null)
			item.getAsJsonArray("items").forEach {
				val holder = Utils.getViewHolder(
					it.asJsonObject,
					(binding.root.context as Activity).layoutInflater,
					binding.root,
					binding.root.context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
				)
				holder.bind(it.asJsonObject, userData)
				binding.root.addView(holder.itemView)
			}
	}
}