package dev.kuylar.lighttube.api.models

import android.text.Html
import com.bumptech.glide.Glide
import dev.kuylar.lighttube.Utils
import dev.kuylar.lighttube.api.models.renderers.IRendererData
import dev.kuylar.lighttube.api.models.renderers.RendererContainer
import dev.kuylar.lighttube.databinding.LayoutChannelHeaderBinding

data class LightTubeChannel(
	val header: Header? = null,
	val tabs: List<ChannelTab>,
	val metadata: ChannelMetadata? = null,
	val contents: List<RendererContainer>
): IRendererData {
	data class Header(
		val id: String,
		val avatars: List<LightTubeImage>,
		val banner: List<LightTubeImage>,
		val tvBanner: List<LightTubeImage>,
		val mobileBanner: List<LightTubeImage>,
		val badges: List<LightTubeBadge>,
		val primaryLink: String? = null,
		val secondaryLink: String? = null,
		val subscriberCountText: String,
		val subscriberCount: Int,
		val title: String,
		val handle: String,
		val videoCountText: String,
		val videoCount: Int,
		val tagline: String?
	)

	data class ChannelTab(
		val tab: String,
		val title: String,
		val params: String,
		val selected: Boolean
	)

	data class ChannelMetadata(
		val id: String,
		val title: String,
		val description: String? = null,
		val handle: String? = null,
		val rssUrl: String,
		val channelUrl: String,
		val keywords: String,
		val availableCountryCodes: List<String>,
		val avatarUrl: String
	)

	fun fillBinding(binding: LayoutChannelHeaderBinding, userData: UserData?) {
		Glide.with(binding.root)
			.load((header?.mobileBanner?.lastOrNull() ?: header?.banner?.lastOrNull())?.url)
			.into(binding.banner)
		Glide.with(binding.root)
			.load(header?.avatars?.lastOrNull()?.url)
			.into(binding.avatar)
		binding.title.text = header?.title
		binding.handle.text = header?.handle
		binding.stats.text = arrayOf(header?.subscriberCountText, header?.videoCountText).joinToString(" • ")
		//todo: onclick
		binding.tagline.text = header?.tagline
		binding.links.text = arrayOf(header?.primaryLink, header?.secondaryLink).joinToString(" ") { Html.fromHtml(it, Html.FROM_HTML_MODE_LEGACY) }
		if (userData != null && userData.channels.containsKey(metadata?.id))
			Utils.updateSubscriptionButton(
				binding.root.context,
				binding.buttonSubscribe,
				userData.channels[metadata?.id]!!
			)
	}

	fun getAsRenderer(): RendererContainer {
		return RendererContainer(
			"channelInfoRenderer",
			"channelInfoRenderer",
			this
		)
	}
}