package dev.kuylar.lighttube.api.models.renderers

data class RendererContainer(
	val type: String,
	val originalType: String,
	val data: IRendererData
) {
	val extras: HashMap<String, Any?> = HashMap()
}