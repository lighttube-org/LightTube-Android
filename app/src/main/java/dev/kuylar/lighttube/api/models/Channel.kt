package dev.kuylar.lighttube.api.models

class Channel(
	val id: String,
	val title: String,
	val handle: String?,
	val avatar: List<LightTubeImage>? = null,
	val subscribersText: String? = null,
	val subscribers: Int? = null,
	val badges: ArrayList<LightTubeBadge>? = null
)
