package dev.kuylar.lighttube.api.models

class UserPlaylist(
	val id: String,
	val name: String,
	val description: String,
	val visibility: PlaylistVisibility,
	val videoIds: List<String>,
	val author: String,
	val lastUpdated: String
)