package dev.kuylar.lighttube.api.models

import android.view.View
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import dev.kuylar.lighttube.Utils
import dev.kuylar.lighttube.databinding.LayoutChannelHeaderBinding

class LightTubeChannel(
	val id: String,
	val title: String,
	val avatars: List<LightTubeImage>,
	val banner: List<LightTubeImage>,
	val badges: List<LightTubeBadge>,
	val primaryLinks: List<ChannelLink>,
	val secondaryLinks: List<ChannelLink>,
	val subscriberCountText: String,
	val enabledTabs: List<String>,
	val contents: List<JsonObject>,
	val continuation: String? = null
) {
	fun fillBinding(binding: LayoutChannelHeaderBinding, userData: UserData?) {
		Glide.with(binding.root)
			.load(banner.lastOrNull()?.url)
			.into(binding.banner)
		Glide.with(binding.root)
			.load(avatars.lastOrNull()?.url)
			.into(binding.avatar)
		binding.title.text = title
		binding.handle.visibility = View.GONE // todo: LTv3 (APIv2)
		binding.stats.text = arrayOf(subscriberCountText).joinToString(" • ") // todo: LTv3 (APIv2)
		//todo: onclick
		binding.tagline.visibility = View.GONE //todo: LTv3 (APIv2)
		binding.links.text = arrayOf(primaryLinks, secondaryLinks).flatMap { it }.joinToString(" ")
		if (userData != null && userData.channels.containsKey(id))
			Utils.updateSubscriptionButton(
				binding.root.context,
				binding.buttonSubscribe,
				userData.channels[id]!!
			)
	}

	fun getAsRenderer(): JsonObject {
		val gson = Gson()
		val asJson = gson.fromJson(gson.toJson(this), JsonObject::class.java)
		asJson.add("type", JsonPrimitive("channelInfoRenderer"))
		return asJson
	}
}