package dev.kuylar.lighttube.api.models.renderers

data class UnknownRendererData(
	val protobufBytes: List<Byte>?,
	val json: String?
) : IRendererData