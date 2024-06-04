package dev.kuylar.lighttube.api.models

import com.google.gson.annotations.JsonAdapter
import dev.kuylar.lighttube.gson.serialization.MotdDeserializer

class InstanceInfo (
	val type: String,
	val version: String,
	@JsonAdapter(MotdDeserializer::class) val motd: List<String>,
	val allowsApi: Boolean,
	val allowsNewUsers: Boolean,
	val allowsOauthApi: Boolean,
	val allowsThirdPartyProxyUsage: Boolean
)
