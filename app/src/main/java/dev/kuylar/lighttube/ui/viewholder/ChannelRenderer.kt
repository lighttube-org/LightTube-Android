package dev.kuylar.lighttube.ui.viewholder

import android.app.Activity
import android.os.Bundle
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.Utils
import dev.kuylar.lighttube.databinding.RendererChannelBinding

class ChannelRenderer(val binding: RendererChannelBinding) : RendererViewHolder(binding.root) {
	override fun bind(item: JsonObject) {
		val context = (binding.root.context as Activity)
		binding.channelTitle.text = item.getAsJsonPrimitive("title").asString
		binding.channelSubtitle.text = context.getString(
			R.string.template_channel_subtitle,
			item.getAsJsonPrimitive("userHandle").asString,
			item.getAsJsonPrimitive("subscriberCountText").asString
		)
		Glide
			.with(binding.root)
			.load(Utils.getBestImageUrlJson(item.getAsJsonArray("avatars")))
			.into(binding.channelAvatar)

		binding.root.setOnClickListener {
			val b = Bundle()
			b.putString("id", item.getAsJsonPrimitive("id").asString)
			context.findNavController(R.id.nav_host_fragment_activity_main)
				.navigate(R.id.navigation_channel, b)
		}
	}
}