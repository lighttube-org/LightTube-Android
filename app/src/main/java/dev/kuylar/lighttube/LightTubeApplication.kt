package dev.kuylar.lighttube

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import androidx.media3.common.util.UnstableApi
import com.google.android.material.color.DynamicColors
import dev.kuylar.lighttube.service.VideoDownloadService
import dev.kuylar.lighttube.ui.activity.CrashHandlerActivity


class LightTubeApplication : Application() {
	@UnstableApi
	override fun onCreate() {
		super.onCreate()

		// Apply dynamic color
		DynamicColors.applyToActivitiesIfAvailable(this)

		// Handle app crashes
		Thread.setDefaultUncaughtExceptionHandler { _, e ->
			val intent = Intent(applicationContext, CrashHandlerActivity::class.java)
			intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
			intent.putExtra("trace", e.stackTraceToString())
			startActivity(intent)
		}

		// Create download manager notification channel
		val mChannel = NotificationChannel(VideoDownloadService.CHANNEL_ID, getString(R.string.nc_download_name), NotificationManager.IMPORTANCE_LOW)
		mChannel.description = getString(R.string.nc_download_desc)
		val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
		notificationManager.createNotificationChannel(mChannel)
	}
}