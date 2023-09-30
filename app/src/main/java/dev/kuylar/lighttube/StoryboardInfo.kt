package dev.kuylar.lighttube

import dev.kuylar.lighttube.ui.StoryboardTransformation
import java.net.URLDecoder
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.truncate

class StoryboardInfo(
	levelsString: String,
	var recommendedLevel: String,
	videoLength: Long
) {
	private val levels: HashMap<String, String> = HashMap()
	private val storyboardCount: Double
	private val secondsPerIndex: Double
	private val secondsPerFrame: Double
	val msPerFrame: Double
	private var indexes = ArrayList<Pair<Int, Int>>()

	init {
		levelsString.split("&").forEach {
			val sp = it.split("=")
			val k = URLDecoder.decode(sp[0], Charsets.UTF_8.name())
			val v = URLDecoder.decode(sp[1], Charsets.UTF_8.name())

			levels[k] = v
		}
		storyboardCount = if (videoLength < 250) {
			ceil((videoLength / 2) / 25.0)
		} else if (videoLength in 250 ..1000) {
			ceil((videoLength / 4) / 25.0)
		} else {
			ceil((videoLength / 10) / 25.0)
		} - 1

		if (recommendedLevel == "2") {
			secondsPerIndex = 125.0
			secondsPerFrame = 5.0
			msPerFrame = 5000.0

			val frameCount = floor(videoLength / 5f).toInt() + 1
			val indexCount = ceil(frameCount / 25f).toInt()

			val lastIndexFrameCount = if (frameCount % 25 == 0) 25 else frameCount % 25

			val lastIndexRows = ceil(lastIndexFrameCount / 5f).toInt()
			val lastIndexColumns = if (lastIndexRows > 1) 5 else frameCount % 5

			for (i in 1 until indexCount) {
				indexes.add(Pair(5, 5))
			}
			indexes.add(Pair(lastIndexRows, lastIndexColumns))
		} else {
			secondsPerIndex = videoLength.toDouble()
			secondsPerFrame = secondsPerIndex / 100
			msPerFrame = secondsPerFrame * 1000

			indexes.add(Pair(10, 10))
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
		val index = indexes[getStoryboardIndex(position)]
		val n = position / 1000 / secondsPerIndex
		val xy = if (recommendedLevel == "2") {
			val positionInFrame = (n - truncate(n)) * 25
			Pair(floor(positionInFrame % 5).toInt(), floor(positionInFrame / 5).toInt())
		} else {
			val positionInFrame = (n - truncate(n)) * 100
			Pair(floor(positionInFrame % 10).toInt(), floor(positionInFrame / 10).toInt())
		}
		return StoryboardTransformation(xy.first, xy.second, index.first, index.second)
	}
}