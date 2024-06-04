package dev.kuylar.lighttube.api.models

data class Caption(
	val vssId: String,
	val languageCode: String,
	val label: String,
	val baseUrl: String,
	val isAutomaticCaption: Boolean
)
