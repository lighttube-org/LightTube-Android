package dev.kuylar.lighttube.api.models

import dev.kuylar.lighttube.api.models.renderers.RendererContainer

data class LightTubeVideoPlaylist(
	val playlistId: String,
	val title: String,
	val totalVideos: Int,
	val currentIndex: Int,
	val localCurrentIndex: Int,
	val channel: Channel,
	val isCourse: Boolean,
	val isInfinite: Boolean,
	val videos: ArrayList<RendererContainer>
)
