package dev.kuylar.lighttube.api.models

class InstanceInfo (
	val type: String,
	val version: String,
	val motd: String,
	val allowsAPI: Boolean,
	val allowsNewUsers: Boolean,
	val allowsOauthAPI: Boolean,
	val allowsThirdPartyProxyUsage: Boolean
)