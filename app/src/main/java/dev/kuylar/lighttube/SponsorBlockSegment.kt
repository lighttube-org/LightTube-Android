package dev.kuylar.lighttube

import com.github.vkay94.timebar.YouTubeSegment
import kotlin.math.roundToLong

class SponsorBlockSegment(
	val category: String,
	val actionType: String,
	segment: List<Double>,
	val uuid: String,
	val videoDuration: Double,
	val locked: Long,
	val votes: Long,
	val description: String
) : YouTubeSegment {
	override var color = 0xFFFFFF
	override val endTimeMs = (segment[1] * 1000).roundToLong()
	override val startTimeMs = (segment[0] * 1000).roundToLong()
}