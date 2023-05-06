package dev.kuylar.lighttube.api.models

class UserData(
	val user: LightTubeUserInfo,
	val channels: HashMap<String, SubscriptionInfo>
)