package dev.kuylar.lighttube.api.models

class ApiResponse<T>(
	val status: String,
	val error: LightTubeException? = null,
	val data: T?,
	val userData: UserData?
)