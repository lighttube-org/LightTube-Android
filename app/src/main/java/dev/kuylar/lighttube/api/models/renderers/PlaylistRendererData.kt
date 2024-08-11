package dev.kuylar.lighttube.api.models.renderers

import dev.kuylar.lighttube.api.models.Channel
import dev.kuylar.lighttube.api.models.LightTubeImage

data class PlaylistRendererData(
	val playlistId: String,
	val thumbnails: List<LightTubeImage>,
	val title: String,
	val videoCountText: String,
	val videoCount: Long,
	val sidebarThumbnails: List<List<LightTubeImage>>?,
	val author: Channel?,
	val childVideos: List<RendererContainer>?,
	val firstVideoId: String?
) : IRendererData