package dev.kuylar.lighttube.api.models

import com.google.gson.annotations.SerializedName

data class EndscreenItem(
	val type: Type,
	val title: String,
	val image: ArrayList<LightTubeImage>,
	val metadata: String,
	val startMs: Long,
	val endMs: Long,
	val aspectRatio: Double,
	val left: Double,
	val top: Double,
	val width: Double,
	val target: String
) {
	enum class Type {
		@SerializedName("Video") Video,
		@SerializedName("Playlist") Playlist,
		@SerializedName("Subscribe") Subscribe,
		@SerializedName("Channel") Channel,
		@SerializedName("Link") Link
	}
}