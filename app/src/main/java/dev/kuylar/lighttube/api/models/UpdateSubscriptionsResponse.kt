package dev.kuylar.lighttube.api.models

class UpdateSubscriptionsResponse(
	val channelName: String,
	val channelAvatar: String,
	val subscribed: Boolean,
	val notifications: Boolean
)