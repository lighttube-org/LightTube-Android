package dev.kuylar.lighttube.api.models.renderers

data class ExceptionRendererData(
	val message: String,
	val rendererCase: String
) : IRendererData