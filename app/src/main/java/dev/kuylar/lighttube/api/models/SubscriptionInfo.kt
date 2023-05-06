package dev.kuylar.lighttube.api.models

class SubscriptionInfo(
	val subscribed: Boolean,
	val notifications: Boolean
) {
	override fun toString(): String {
		return "Subscribed: $subscribed | Notifications: $notifications"
	}
}