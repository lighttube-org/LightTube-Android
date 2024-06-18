package dev.kuylar.lighttube.api.models.renderers

import dev.kuylar.lighttube.api.models.LightTubeImage

data class WatchCardHeroVideoRendererData(
	val title: String,
	val videoId: String?,
	val heroImages: List<List<LightTubeImage>>
) : IRendererData