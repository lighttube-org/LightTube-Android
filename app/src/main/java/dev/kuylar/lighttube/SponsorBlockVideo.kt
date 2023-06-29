package dev.kuylar.lighttube

import com.google.gson.annotations.SerializedName

class SponsorBlockVideo(
	@SerializedName("videoID") val videoId: String,
	val segments: List<SponsorBlockSegment>
)