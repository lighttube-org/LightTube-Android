package dev.kuylar.lighttube.api.models

import com.google.gson.JsonObject

class SearchResults(
	val searchResults: List<JsonObject>,
	val searchFilters: List<FilterGroup>,
	val estimatedResultCount: Long,
	val continuationKey: String,
	val typoFixer: TypoFixer
) {
	class FilterGroup(
		val title: String,
		val filters: List<Filter>
	)

	class Filter(
		val label: String,
		val params: String,
		val tooltip: String,
		val selected: Boolean
	)

	class TypoFixer(
		val originalQuery: String,
		val correctedQuery: String,
		val params: String
	)
}