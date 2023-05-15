package dev.kuylar.lighttube

import android.app.Application
import android.content.Intent
import com.google.android.material.color.DynamicColors
import dev.kuylar.lighttube.ui.activity.CrashHandlerActivity


class LightTubeApplication : Application() {
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
	}
}