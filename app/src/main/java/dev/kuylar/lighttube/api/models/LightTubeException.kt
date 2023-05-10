package dev.kuylar.lighttube.api.models

import java.lang.Exception

class LightTubeException(
	val code: Int,
	override val message: String
): Exception()