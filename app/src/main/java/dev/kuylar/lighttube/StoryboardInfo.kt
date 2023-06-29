package dev.kuylar.lighttube

import android.util.Log
import dev.kuylar.lighttube.ui.StoryboardTransformation
import java.lang.Exception
import kotlin.math.ceil
import java.net.URLDecoder
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.truncate

class StoryboardInfo(
	levelsString: String,
	val recommendedLevel: String,
	videoLength: Long
) {
	val levels: HashMap<String, String> = HashMap()
	val storyboardCount: Double
	val secondsPerIndex: Double
	val secondsPerFrame: Double
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

		for (i in 0..(storyboardCount - 1).toInt()) {
			Log.i("VideoPlayerManager", levels["2"]!!.replace("M0", "M$i"))
		}

		secondsPerIndex = videoLength / storyboardCount
		secondsPerFrame = secondsPerIndex
	}

	fun getImageUrl(position: Long): String {
		return levels["2"]!!.replace("M0", "M${getStoryboardIndex(position)}")
	}

	private fun getStoryboardIndex(position: Long): Int {
		return floor((position / 1000) / secondsPerIndex).toInt()
	}

	fun getTransformation(position: Long): StoryboardTransformation {
		val n = position / 1000 / secondsPerIndex
		val positionInFrame = (n - truncate(n)) * 25
		val x = floor(positionInFrame % 5).toInt()
		val y = floor(positionInFrame / 5).toInt()
		Log.i("Storyboard", "pif: $positionInFrame x: $x, y: $y")
		return StoryboardTransformation(x, y, 100, 50)
	}

	fun throttle(position: Long) {
		Log.i("Storyboard", "Difference: ${abs(lastPosition - position)}")
		if (lastPosition == -1L) {
			lastPosition = position
			return
		}
		if (abs(lastPosition - position) < secondsPerFrame * 40) throw Exception("throttle")
		lastPosition = position
	}
}