package dev.kuylar.lighttube.api.models

class Channel(
	val id: String,
	val title: String,
	val avatar: String? = null,
	val subscribers: String? = null,
	val badges: ArrayList<LightTubeBadge>
)
