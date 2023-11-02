package dev.kuylar.lighttube.api.models

class UserData(
	var user: LightTubeUserInfo?,
	val channels: HashMap<String, SubscriptionInfo>
)