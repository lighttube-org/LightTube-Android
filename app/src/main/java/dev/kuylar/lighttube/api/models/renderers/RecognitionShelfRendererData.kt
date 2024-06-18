package dev.kuylar.lighttube.api.models.renderers

data class RecognitionShelfRendererData(
	val title: String,
	val subtitle: String,
	val avatars: List<String>
): IRendererData