package dev.kuylar.lighttube.api.models.renderers

import dev.kuylar.lighttube.api.models.LightTubeBadge
import dev.kuylar.lighttube.api.models.LightTubeImage

data class ChannelRendererData(
	val channelId: String,
	val title: String,
	val handle: String?,
	val avatar: List<LightTubeImage>,
	val videoCountText: String?,
	val videoCount: Long,
	val subscriberCountText: String?,
	val subscriberCount: Long,
	val badges: List<LightTubeBadge>
): IRendererData