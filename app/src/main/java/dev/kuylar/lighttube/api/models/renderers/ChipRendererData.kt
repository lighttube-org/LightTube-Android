package dev.kuylar.lighttube.api.models.renderers

data class ChipRendererData(
	val title: String,
	val continuationToken: String?,
	val params: String?,
	val isSelected: Boolean
) : IRendererData
