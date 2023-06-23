package dev.kuylar.lighttube.api.models

import com.google.gson.JsonObject

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
)