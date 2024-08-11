package dev.kuylar.lighttube.api.models

import dev.kuylar.lighttube.api.LightTubeApi
import dev.kuylar.lighttube.api.models.renderers.IRendererData
import dev.kuylar.lighttube.api.models.renderers.RendererContainer

data class LightTubePlaylist(
	val id: String,
	val alerts: List<String>,
	val contents: List<RendererContainer>,
	val chips: List<RendererContainer>,
	val continuation: String? = null,
	val sidebar: Sidebar
): IRendererData {
	data class Sidebar(
		val title: String,
		val thumbnails: List<LightTubeImage>,
		val videoCountText: String,
		val videoCount: Long,
		val viewCountText: String,
		val viewCount: Long,
		val lastUpdated: String,
		val description: String? = null,
		val channel: Channel
	)

	var editable: Boolean = false

	fun getAsRenderer(api: LightTubeApi) : RendererContainer {
		editable = api.currentUser?.ltChannelID == sidebar.channel.id
		return RendererContainer(
			"playlistInfoRenderer",
			"playlistInfoRenderer",
			this
		)
	}
}