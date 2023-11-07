package dev.kuylar.lighttube.api.models

class UserData(
	var user: LightTubeUserInfo?,
	val channels: HashMap<String, SubscriptionInfo>,
	var editable: Boolean, ///todo: send this from the API in the future
	var playlistId: String?
)