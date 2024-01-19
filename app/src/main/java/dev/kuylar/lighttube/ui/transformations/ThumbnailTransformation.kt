package dev.kuylar.lighttube.ui.transformations

import android.graphics.Bitmap
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest

class ThumbnailTransformation : BitmapTransformation() {
	override fun transform(
		pool: BitmapPool,
		toTransform: Bitmap,
		outWidth: Int,
		outHeight: Int
	): Bitmap =
		Bitmap.createBitmap(
			toTransform,
			0,
			toTransform.height / 8,
			toTransform.width,
			toTransform.height - (toTransform.height / 4)
		)

	override fun updateDiskCacheKey(messageDigest: MessageDigest) {
		messageDigest.update("thumbnailTransformation".encodeToByteArray())
	}
}
