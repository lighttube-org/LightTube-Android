package dev.kuylar.lighttube.service

import android.app.Notification
import android.graphics.drawable.Icon
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.PlatformScheduler
import com.google.gson.Gson
import dev.kuylar.lighttube.BuildConfig
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.database.models.DownloadInfo


@UnstableApi
class VideoDownloadService : DownloadService(NOTIFICATION_ID, 100L, CHANNEL_ID, R.string.nc_download_name, R.string.nc_download_desc) {
	private val gson = Gson()
	override fun getDownloadManager() =
		dev.kuylar.lighttube.DownloadManager.getDownloadManager(applicationContext)

	override fun getScheduler() = if (Util.SDK_INT >= 21) PlatformScheduler(this, JOB_ID) else null

	override fun getForegroundNotification(
		downloads: MutableList<Download>,
		notMetRequirements: Int
	) = Notification.Builder(applicationContext, CHANNEL_ID).apply {
		if (downloads.isNotEmpty()) {
			val dl = downloads.first()
			val info = gson.fromJson(dl.request.data.decodeToString(), DownloadInfo::class.java)
			this.setContentTitle("Downloading " + info.title)
			this.setProgress(
				1000,
				(dl.percentDownloaded * 10).toInt(),
				dl.percentDownloaded == 0f
			)
			if (downloads.size > 1)
				this.setSubText("${downloads.size} videos are in the queue")
		} else {
			this.setContentTitle("Downloading videos...")
			this.setProgress(1, 0, true)
		}
		this.setSmallIcon(Icon.createWithResource(applicationContext, R.drawable.ic_download))
	}.build()

	companion object {
		const val NOTIFICATION_ID = 519693180 // made by punching the numpad 3 times
		const val JOB_ID = 5131871 // also made by punching the numpad. 2 times this time tho
		const val CHANNEL_ID = "${BuildConfig.APPLICATION_ID}.service.VideoDownloadService"
	}
}