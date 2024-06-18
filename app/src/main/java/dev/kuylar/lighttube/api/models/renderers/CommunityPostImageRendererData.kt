package dev.kuylar.lighttube.api.models.renderers

import dev.kuylar.lighttube.api.models.LightTubeImage

data class CommunityPostImageRendererData(
	val images: List<List<LightTubeImage>>
): IRendererData