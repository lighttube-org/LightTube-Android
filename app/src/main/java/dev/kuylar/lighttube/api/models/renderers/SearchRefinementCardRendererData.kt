package dev.kuylar.lighttube.api.models.renderers

data class SearchRefinementCardRendererData(
	val thumbnail: String,
	val title: String,
	val playlistId: String?,
	val searchQuery: String?
) : IRendererData