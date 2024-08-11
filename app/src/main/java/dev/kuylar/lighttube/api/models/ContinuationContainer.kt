package dev.kuylar.lighttube.api.models

class ContinuationContainer<T>(
	val results: List<T>,
	val continuationToken: String?
)
