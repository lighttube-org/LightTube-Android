package dev.kuylar.lighttube.api.models

data class SearchSuggestions(
	val query: String,
	val autocomplete: List<String>
)