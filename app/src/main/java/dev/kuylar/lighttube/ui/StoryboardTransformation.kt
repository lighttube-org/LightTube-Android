package dev.kuylar.lighttube.ui

import android.graphics.Bitmap
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.nio.charset.Charset
import java.security.MessageDigest


class StoryboardTransformation(
	private val x: Int,
	private val y: Int,
	private val w: Int,
	private val h: Int
) :
	BitmapTransformation() {
	private fun getCacheKey(): String =
		"dev.kuylar.lighttube.ui.StoryboardTransformation($x,$y,$w,$h)"

	override fun transform(
		pool: BitmapPool,
		toTransform: Bitmap,
		outWidth: Int,
		outHeight: Int
	): Bitmap = Bitmap.createBitmap(
		toTransform,
		x * w,
		y * h,
		w,
		h
	)


	override fun hashCode(): Int {
		return getCacheKey().hashCode()
	}

	override fun equals(other: Any?): Boolean {
		return if (other is StoryboardTransformation) {
			other.x == x && other.y == y && other.w == w && other.h == h
		} else false
	}

	override fun updateDiskCacheKey(messageDigest: MessageDigest) {
		messageDigest.update(getCacheKey().toByteArray(Charset.forName("UTF-8")))
	}
}