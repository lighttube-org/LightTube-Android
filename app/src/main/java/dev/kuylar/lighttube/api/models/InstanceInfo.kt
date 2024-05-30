package dev.kuylar.lighttube.api.models

class InstanceInfo (
	val type: String,
	val version: String,
	val motd: String,
	val allowsApi: Boolean,
	val allowsNewUsers: Boolean,
	val allowsOauthApi: Boolean,
	val allowsThirdPartyProxyUsage: Boolean
)