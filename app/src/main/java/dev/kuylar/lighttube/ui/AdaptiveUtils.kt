package dev.kuylar.lighttube.ui

import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigationrail.NavigationRailView

object AdaptiveUtils {
	private const val MEDIUM_SCREEN_WIDTH_SIZE = 600
	private const val LARGE_SCREEN_WIDTH_SIZE = 1240
	private var areNavsEnabled = true

	fun updateNavLayout(
		screenWidth: Int,
		bottomNav: BottomNavigationView,
		navRail: NavigationRailView
	) {
		if (screenWidth < MEDIUM_SCREEN_WIDTH_SIZE) {
			// Small screen
			bottomNav.visibility = if (areNavsEnabled) View.VISIBLE else View.GONE
			navRail.visibility = View.GONE
		} else {
			// Medium screen
			bottomNav.visibility = View.GONE
			navRail.visibility = if (areNavsEnabled) View.VISIBLE else View.GONE
		}
	}

	fun toggleNavBars(
		hide: Boolean,
		screenWidth: Int,
		bottomNav: BottomNavigationView,
		navRail: NavigationRailView
	) {
		areNavsEnabled = hide
		if (hide) {
			bottomNav.visibility = View.GONE
			navRail.visibility = View.GONE
		} else {
			updateNavLayout(screenWidth, bottomNav, navRail)
		}
	}
}