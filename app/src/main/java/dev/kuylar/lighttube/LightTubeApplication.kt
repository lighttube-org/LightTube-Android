package dev.kuylar.lighttube

import android.app.Application
import com.google.android.material.color.DynamicColors

class LightTubeApplication: Application() {
	override fun onCreate() {
		super.onCreate()

		// Apply dynamic color
		DynamicColors.applyToActivitiesIfAvailable(this)
	}
}