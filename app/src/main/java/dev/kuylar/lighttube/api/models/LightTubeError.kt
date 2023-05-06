package dev.kuylar.lighttube.api.models

import java.lang.Exception

class LightTubeError(
	val message: String,
	val code: Long
) {
	fun getException(): Exception {
		return Exception("[$code] $message")
	}
}