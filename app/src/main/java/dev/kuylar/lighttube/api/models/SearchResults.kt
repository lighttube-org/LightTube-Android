package dev.kuylar.lighttube.api.models

import dev.kuylar.lighttube.api.models.renderers.RendererContainer

data class SearchResults(
	val searchResults: List<RendererContainer>,
	val queryCorrector: QueryCorrector? = null,
	val chips: List<RendererContainer>,
	val continuation: String?,
	val refinements: List<String>,
	val estimatedResults: Long,
	val searchParams: Params,
) {
	data class QueryCorrector(
		val correctionType: Int,
		val originalQuery: String? = null,
		val correctedQuery: String
	)

	data class Params(
		val sortBy: Int,
		val filters: Filters,
		val queryFlags: Flags,
		val index: Int
	)

	data class Filters(
		val uploadedIn: Int,
		val type: Int,
		val duration: Int,
		val hd: Boolean,
		val subtitles: Boolean,
		val creativeCommons: Boolean,
		val resolution3D: Boolean,
		val live: Boolean,
		val purchased: Boolean,
		val resolution4K: Boolean,
		val vr360: Boolean,
		val location: Boolean,
		val hdr: Boolean,
		val vr180: Boolean
	)

	data class Flags(
		val exactSearch: Boolean
	)
}