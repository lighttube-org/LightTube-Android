package dev.kuylar.lighttube.api.models.renderers

data class ContainerRendererData(
	val items: List<RendererContainer>,
	val style: String,
	val title: String,
	val subtitle: String?,
	val destination: String?,
	val shownItemCount: Int?
) : IRendererData