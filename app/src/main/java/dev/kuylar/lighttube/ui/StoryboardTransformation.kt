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
	private val ID = "dev.kuylar.lighttube.ui.StoryboardTransformation"
	private val ID_BYTES = ID.toByteArray(Charset.forName("UTF-8"))

	override fun transform(
		pool: BitmapPool,
		toTransform: Bitmap,
		outWidth: Int,
		outHeight: Int
	): Bitmap = Bitmap.createBitmap(
		toTransform,
		x * (toTransform.width / 5),
		y * 90, // hardcoded cus youtube sometimes doesnt has < 5 rows
		w,
		h
	)

	override fun hashCode(): Int {
		return ID.hashCode()
	}

	override fun updateDiskCacheKey(messageDigest: MessageDigest) {
		messageDigest.update(ID_BYTES)
	}
}