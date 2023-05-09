package dev.kuylar.lighttube.api.models

class SearchSuggestions(
	val query: String,
	val autocomplete: List<String>
)