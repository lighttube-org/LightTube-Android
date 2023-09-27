package dev.kuylar.lighttube

import dev.kuylar.lighttube.ui.StoryboardTransformation
import java.net.URLDecoder
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.truncate

class StoryboardInfo(
	levelsString: String,
	var recommendedLevel: String,
	videoLength: Long
) {
	val levels: HashMap<String, String> = HashMap()
	val storyboardCount: Double
	val secondsPerIndex: Double
	val secondsPerFrame: Double
	val msPerFrame: Double
	var lastPosition = -1L

	init {
		levelsString.split("&").forEach {
			val sp = it.split("=")
			val k = URLDecoder.decode(sp[0], Charsets.UTF_8.name())
			val v = URLDecoder.decode(sp[1], Charsets.UTF_8.name())

			levels[k] = v
		}
		storyboardCount = if (videoLength < 250) {
			ceil((videoLength / 2) / 25.0)
		} else if (videoLength in 250..1000) {
			ceil((videoLength / 4) / 25.0)
		} else {
			ceil((videoLength / 10) / 25.0)
		} - 1

		if (recommendedLevel == "2") {
			secondsPerIndex = 125.0
			secondsPerFrame = secondsPerIndex / 25
			msPerFrame = secondsPerFrame * 1000
		} else {
			secondsPerIndex = videoLength.toDouble()
			secondsPerFrame = secondsPerIndex / 100
			msPerFrame = secondsPerFrame * 1000
		}
	}

	fun getImageUrl(position: Long): String {
		return if (recommendedLevel == "2")
			levels["2"]?.replace("M0", "M${getStoryboardIndex(position)}") ?: ""
		else
			levels["0"] ?: ""
	}

	private fun getStoryboardIndex(position: Long): Int {
		return if (recommendedLevel == "2")
			floor((position / 1000) / secondsPerIndex).toInt()
		else
			0
	}

	fun getTransformation(position: Long): StoryboardTransformation {
		return if (recommendedLevel == "2") {
			val n = position / 1000 / secondsPerIndex
			val positionInFrame = (n - truncate(n)) * 25
			val x = floor(positionInFrame % 5).toInt()
			val y = floor(positionInFrame / 5).toInt()
			StoryboardTransformation(x, y, 96, 54)
		} else {
			val n = position / 1000 / secondsPerIndex
			val positionInFrame = (n - truncate(n)) * 100
			val x = floor(positionInFrame % 10).toInt()
			val y = floor(positionInFrame / 10).toInt()
			StoryboardTransformation(x, y, 48, 27)
		}
	}

	fun throttle(position: Long): Boolean {
		if (lastPosition == -1L) {
			lastPosition = position
			return true
		}
		if (abs(lastPosition - position) < msPerFrame) return false
		lastPosition = position
		return true
	}
}