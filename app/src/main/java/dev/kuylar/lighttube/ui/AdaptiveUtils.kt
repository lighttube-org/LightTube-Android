package dev.kuylar.lighttube.ui

import android.content.Context
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.navigationrail.NavigationRailView
import dev.kuylar.lighttube.R
import kotlin.math.ceil
import kotlin.math.round

object AdaptiveUtils {
	private const val MEDIUM_SCREEN_WIDTH_SIZE = 600
	private const val LARGE_SCREEN_WIDTH_SIZE = 1240
	private var areNavsEnabled = true

	const val ITEM_TYPE_VIDEO = 0
	const val ITEM_TYPE_SHORTS = 1
	const val ITEM_TYPE_SEARCH = 2

	fun updateNavLayout(
		context: Context,
		screenWidth: Int,
		bottomNav: BottomNavigationView,
		navRail: NavigationRailView,
		miniplayer: BottomSheetBehavior<View>? = null
	) {
		if (screenWidth < MEDIUM_SCREEN_WIDTH_SIZE) {
			// Small screen
			bottomNav.visibility = if (areNavsEnabled) View.VISIBLE else View.GONE
			navRail.visibility = View.GONE
			miniplayer?.setPeekHeight(context.resources.getDimensionPixelOffset(R.dimen.miniplayer_peek_with_navbar), false)
		} else {
			// Medium screen
			bottomNav.visibility = View.GONE
			navRail.visibility = if (areNavsEnabled) View.VISIBLE else View.GONE
			miniplayer?.setPeekHeight(context.resources.getDimensionPixelOffset(R.dimen.miniplayer_peek_without_navbar), false)
		}
	}

	fun toggleNavBars(
		context: Context,
		hide: Boolean,
		screenWidth: Int,
		bottomNav: BottomNavigationView,
		navRail: NavigationRailView,
		miniplayer: BottomSheetBehavior<View>
	) {
		areNavsEnabled = !hide
		if (hide) {
			bottomNav.visibility = View.GONE
			navRail.visibility = View.GONE
		} else {
			updateNavLayout(context, screenWidth, bottomNav, navRail)
		}
	}

	fun getColumnCount(
		recyclerWidth: Float,
		itemType: Int
	): Int {
		if (recyclerWidth < MEDIUM_SCREEN_WIDTH_SIZE) return 1

		val itemSize = when(itemType) {
			ITEM_TYPE_VIDEO -> 240
			else -> Int.MAX_VALUE
		}

		return round(ceil(recyclerWidth / itemSize)).toInt()
	}
}