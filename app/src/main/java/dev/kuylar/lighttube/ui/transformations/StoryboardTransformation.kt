package dev.kuylar.lighttube.ui.transformations

import android.graphics.Bitmap
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.nio.charset.Charset
import java.security.MessageDigest


class StoryboardTransformation(
	private val x: Int,
	private val y: Int,
	private val r: Int,
	private val c: Int
) :
	BitmapTransformation() {
	private fun getCacheKey(): String =
		"dev.kuylar.lighttube.ui.transformations.StoryboardTransformation($x,$y,$r,$c)"

	override fun transform(
		pool: BitmapPool,
		toTransform: Bitmap,
		outWidth: Int,
		outHeight: Int
	): Bitmap = Bitmap.createBitmap(
		toTransform,
		x * toTransform.width / c,
		y * toTransform.height / r,
		toTransform.width / c,
		toTransform.height / r
	)


	override fun hashCode(): Int {
		return getCacheKey().hashCode()
	}

	override fun equals(other: Any?): Boolean {
		return if (other is StoryboardTransformation) {
			other.x == x && other.y == y && other.r == r && other.c == c
		} else false
	}

	override fun updateDiskCacheKey(messageDigest: MessageDigest) {
		messageDigest.update(getCacheKey().toByteArray(Charset.forName("UTF-8")))
	}
}