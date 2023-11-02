package dev.kuylar.lighttube.ui.viewholder

import android.app.Activity
import android.os.Bundle
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.Utils
import dev.kuylar.lighttube.api.models.SubscriptionInfo
import dev.kuylar.lighttube.api.models.UserData
import dev.kuylar.lighttube.databinding.RendererChannelBinding

class ChannelRenderer(val binding: RendererChannelBinding) : RendererViewHolder(binding.root) {
	override fun bind(item: JsonObject, userData: UserData?) {
		val context = (binding.root.context as Activity)
		val id = item.getAsJsonPrimitive("id").asString
		var subscriptionInfo =
			userData?.channels?.get(id) ?: SubscriptionInfo(
				subscribed = false,
				notifications = false
			)
		binding.channelTitle.text = item.getAsJsonPrimitive("title").asString
		if (item.getAsJsonPrimitive("type").asString == "gridChannelRenderer") {
			binding.channelHandle.text = item.getAsJsonPrimitive("videoCountText").asString
			binding.channelSubscribers.text =
				item.getAsJsonPrimitive("subscriberCountText").asString
		} else {
			binding.channelHandle.text = item.getAsJsonPrimitive("userHandle").asString
			binding.channelSubscribers.text =
				item.getAsJsonPrimitive("subscriberCountText").asString
		}
		Glide
			.with(binding.root)
			.load(Utils.getBestImageUrlJson(item.getAsJsonArray("avatars")))
			.into(binding.channelAvatar)

		Utils.updateSubscriptionButton(
			binding.root.context,
			binding.buttonSubscribe,
			subscriptionInfo
		)

		binding.buttonSubscribe.setOnClickListener {
			Utils.subscribe(context, id, subscriptionInfo, binding.buttonSubscribe) {
				subscriptionInfo = it
			}
		}

		binding.root.setOnClickListener {
			val b = Bundle()
			b.putString("id", id)
			context.findNavController(R.id.nav_host_fragment_activity_main)
				.navigate(R.id.navigation_channel, b)
		}
	}
}