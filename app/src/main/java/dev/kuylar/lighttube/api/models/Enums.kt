package dev.kuylar.lighttube.api.models

import com.google.gson.annotations.SerializedName

enum class SortOrder {
	TopComments,
	NewestFirst
}

enum class PlaylistVisibility {
	@SerializedName("0") Private,
	@SerializedName("1") Unlisted,
	@SerializedName("2") Visible
}