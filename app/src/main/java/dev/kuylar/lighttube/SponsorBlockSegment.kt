package dev.kuylar.lighttube

import com.github.vkay94.timebar.YouTubeSegment
import kotlin.math.roundToLong

class SponsorBlockSegment(
	val category: String,
	val actionType: String,
	val segment: List<Double>,
	val uuid: String?,
	val videoDuration: Double,
	val locked: Long,
	val votes: Long,
	val description: String
) : YouTubeSegment {
	override var color = 0x00000000
	override var endTimeMs: Long = 0
	override var startTimeMs: Long = 0

	// we have to do this cus gson doesnt call init{}
	constructor(
		segment: SponsorBlockSegment
	) : this(
		segment.category,
		segment.actionType,
		segment.segment,
		segment.uuid,
		segment.videoDuration,
		segment.locked,
		segment.votes,
		segment.description
	) {
		this.color = when (category) {
			"sponsor" -> DEFAULT_SPONSOR_COLOR
			"selfpromo" -> DEFAULT_SELFPROMO_COLOR
			"interaction" -> DEFAULT_INTERACTION_COLOR
			"intro" -> DEFAULT_INTRO_COLOR
			"outro" -> DEFAULT_OUTRO_COLOR
			"preview" -> DEFAULT_PREVIEW_COLOR
			"music_offtopic" -> DEFAULT_MUSIC_OFFTOPIC_COLOR
			else -> DEFAULT_SELFPROMO_COLOR
		}
		this.endTimeMs = (segment.segment[1] * 1000).roundToLong()
		this.startTimeMs = (segment.segment[0] * 1000).roundToLong()
	}

	companion object {
		private const val DEFAULT_SPONSOR_COLOR = 0xFF00D400.toInt()
		private const val DEFAULT_SELFPROMO_COLOR = 0xFFFFFF00.toInt()
		private const val DEFAULT_INTERACTION_COLOR = 0xFFCC00FF.toInt()
		private const val DEFAULT_INTRO_COLOR = 0xFF00FFFF.toInt()
		private const val DEFAULT_OUTRO_COLOR = 0xFF0202ED.toInt()
		private const val DEFAULT_PREVIEW_COLOR = 0xFF008FD6.toInt()
		private const val DEFAULT_MUSIC_OFFTOPIC_COLOR = 0xFFFF9900.toInt()
	}
}