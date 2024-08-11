package dev.kuylar.lighttube.ui.viewholder

import android.app.Activity
import android.os.Bundle
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.Utils
import dev.kuylar.lighttube.api.models.SubscriptionInfo
import dev.kuylar.lighttube.api.models.UserData
import dev.kuylar.lighttube.api.models.renderers.ChannelRendererData
import dev.kuylar.lighttube.api.models.renderers.RendererContainer
import dev.kuylar.lighttube.databinding.RendererChannelBinding

class ChannelRenderer(val binding: RendererChannelBinding) : RendererViewHolder(binding.root) {
	override fun bind(renderer: RendererContainer, userData: UserData?) {
		val context = (binding.root.context as Activity)
		val item = renderer.data as ChannelRendererData
		val id = item.channelId
		var subscriptionInfo =
			userData?.channels?.get(id) ?: SubscriptionInfo(
				subscribed = false,
				notifications = false
			)
		binding.channelTitle.text = item.title

		val subscribersText = context.resources.getQuantityString(
			R.plurals.template_subscribers,
			item.subscriberCount.toInt(),
			Utils.toShortInt(context, item.subscriberCount)
		)

		if (item.handle != null) {
			binding.channelHandle.text = item.handle
			binding.channelSubscribers.text = subscribersText
		} else {
			binding.channelHandle.text = subscribersText
			binding.channelSubscribers.text = context.resources.getQuantityString(
				R.plurals.template_videos,
				item.videoCount.toInt(),
				Utils.toShortInt(context, item.videoCount)
			)
		}

		Glide
			.with(binding.root)
			.load(Utils.getBestImageUrl(item.avatar))
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