package dev.kuylar.lighttube.api.models

class ContinuationContainer<T>(
	val contents: List<T>,
	val continuation: String?
)
