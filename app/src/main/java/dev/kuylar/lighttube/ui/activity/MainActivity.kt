package dev.kuylar.lighttube.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.SearchAutoComplete
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.get
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.google.android.material.elevation.SurfaceColors
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.Utils
import dev.kuylar.lighttube.api.LightTubeApi
import dev.kuylar.lighttube.api.models.LightTubeException
import dev.kuylar.lighttube.databinding.ActivityMainBinding
import dev.kuylar.lighttube.ui.VideoPlayerManager
import dev.kuylar.lighttube.ui.fragment.UpdateFragment
import java.io.IOException
import kotlin.concurrent.thread
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class MainActivity : AppCompatActivity() {
	private lateinit var navController: NavController
	private lateinit var binding: ActivityMainBinding
	lateinit var miniplayer: BottomSheetBehavior<View>
	private lateinit var miniplayerScene: MotionLayout
	lateinit var player: VideoPlayerManager
	private lateinit var api: LightTubeApi
	private var loadingSuggestions = false
	private var fullscreen = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// set status bar & bottom nav bar colors
		val color = SurfaceColors.SURFACE_2.getColor(this)
		window.statusBarColor = color
		window.navigationBarColor = color

		val sp = getSharedPreferences("main", MODE_PRIVATE)
		if (!sp.contains("instanceHost")) {
			startActivity(Intent(this, SetupActivity::class.java))
			finish()
			return
		}

		setApi()

		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)

		val navView: BottomNavigationView = binding.navView
		navController = findNavController(R.id.nav_host_fragment_activity_main)
		// Passing each menu ID as a set of Ids because each
		// menu should be considered as top level destinations.
		val appBarConfiguration = AppBarConfiguration(
			setOf(
				R.id.navigation_home, R.id.navigation_subscriptions, R.id.navigation_library
			)
		)

		setSupportActionBar(binding.toolbar)
		setupActionBarWithNavController(navController, appBarConfiguration)
		navView.setupWithNavController(navController)

		onBackPressedDispatcher.addCallback(this) {
			goBack(true)
		}

		val miniplayerView: View = findViewById(R.id.miniplayer)
		miniplayerScene = miniplayerView.findViewById(R.id.miniplayer_motion)
		miniplayer = BottomSheetBehavior.from(miniplayerView)
		miniplayer.state = BottomSheetBehavior.STATE_HIDDEN

		player = VideoPlayerManager(this)

		miniplayer.addBottomSheetCallback(object :
			BottomSheetBehavior.BottomSheetCallback() {
			@SuppressLint("SwitchIntDef")
			override fun onStateChanged(bottomSheet: View, newState: Int) {
				when (newState) {
					BottomSheetBehavior.STATE_HIDDEN -> player.stop()

					BottomSheetBehavior.STATE_DRAGGING -> {
						supportActionBar?.show()
					}

					BottomSheetBehavior.STATE_EXPANDED -> {
						supportActionBar?.hide()
					}
				}
			}

			override fun onSlide(bottomSheet: View, slideOffset: Float) {
				if (slideOffset < 0)
					player.setVolume(max(0.1f, 1 + slideOffset * 3))
				else
					player.setVolume(1f)
				miniplayerScene.progress = max(0f, min(1f, slideOffset * 5))

				if (slideOffset > .3)
					binding.navView.visibility = View.GONE
				else
					binding.navView.visibility = View.VISIBLE

				player.toggleControls(slideOffset * 5 >= 1)
			}
		})

		// check for updates
		thread {
			val update = Utils.checkForUpdates()
			if (update != null) {
				val skippedUpdate = sp.getString("skippedUpdate", null)
				if (skippedUpdate == update.latestVersion) {
					Log.i(
						"UpdateChecker",
						"User skipped update $skippedUpdate. Not showing the update dialog."
					)
				} else {
					runOnUiThread {
						UpdateFragment(update).show(supportFragmentManager, null)
					}
				}
			}
		}

		val handler = Handler(mainLooper)
		var sponsorblockRunnable = Runnable {}
		sponsorblockRunnable = Runnable {
			try {
				player.updateSkipButton(player.getCurrentSegment())
			} catch (e: Exception) {
				Log.e("SponsorBlockLoop", "Failed to update SponsorBlock skip button", e)
			}
			handler.postDelayed(sponsorblockRunnable, 100)
		}
		handler.postDelayed(sponsorblockRunnable, 100)
	}

	private fun goBack(closeApp: Boolean): Boolean {
		if (!player.closeSheets()) // attempt to close details/comments
			if (!tryExitFullscreen()) // attempt to exit fullscreen
				if (!minimizePlayer()) // attempt to minimize the player sheet
					if (!navController.popBackStack()) { // attempt to go back on the fragment history
						// close the app
						if (closeApp) finish()
						return true
					}
		return false
	}

	override fun onSupportNavigateUp(): Boolean {
		return goBack(false)
	}

	private fun setApi() {
		api = LightTubeApi(this)
	}

	fun getApi(): LightTubeApi {
		if (!this::api.isInitialized)
			setApi()
		return api
	}

	private fun minimizePlayer(): Boolean {
		return if (miniplayer.state == BottomSheetBehavior.STATE_EXPANDED) {
			miniplayer.state = BottomSheetBehavior.STATE_COLLAPSED
			true
		} else false
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.top_app_bar, menu)

		val searchView = menu[0].actionView as SearchView
		val searchAutoComplete: SearchAutoComplete =
			searchView.findViewById(androidx.appcompat.R.id.search_src_text) as SearchAutoComplete

		searchAutoComplete.onItemClickListener =
			OnItemClickListener { adapterView, _, itemIndex, _ ->
				val queryString = adapterView.getItemAtPosition(itemIndex) as String
				searchAutoComplete.setText(queryString)
				searchAutoComplete.setSelection(queryString.length)
			}

		searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
			override fun onQueryTextSubmit(query: String): Boolean {
				val bundle = Bundle()
				bundle.putString("query", query)
				navController.navigate(R.id.navigation_search, bundle)
				searchView.isSelected = false
				searchView.clearFocus()
				searchAutoComplete.dismissDropDown()
				return false
			}

			override fun onQueryTextChange(newText: String): Boolean {
				return updateSuggestions(newText, searchAutoComplete)
			}
		})

		return true
	}

	private fun updateSuggestions(
		newText: String,
		searchAutoComplete: SearchAutoComplete
	): Boolean {
		if (loadingSuggestions) return false
		if (newText.isEmpty()) return false
		loadingSuggestions = true
		thread {
			try {
				val suggestions = api.searchSuggestions(newText)
				runOnUiThread {
					val newsAdapter = ArrayAdapter(
						this@MainActivity,
						android.R.layout.simple_dropdown_item_1line,
						suggestions.data!!.autocomplete
					)
					searchAutoComplete.setAdapter(newsAdapter)
					if (searchAutoComplete.isSelected)
						searchAutoComplete.showDropDown()
					loadingSuggestions = false
				}
			} catch (e: LightTubeException) {
				loadingSuggestions = false
			} catch (e: IOException) {
				loadingSuggestions = false
			}
		}
		return true
	}

	fun setLoading(loading: Boolean) {
		try {
			binding.loadingBar.visibility = if (loading) View.VISIBLE else View.GONE
		} catch (_: Exception) {
		}
	}

	fun enterFullscreen(playerView: View, isPortrait: Boolean) {
		fullscreen = true
		val parentToMove = playerView.parent as View
		(parentToMove.parent as ViewGroup).removeView(parentToMove)
		binding.fullscreenPlayerContainer.addView(parentToMove)

		WindowCompat.setDecorFitsSystemWindows(window, false)
		WindowInsetsControllerCompat(window, window.decorView).let { controller ->
			controller.hide(WindowInsetsCompat.Type.systemBars())
			controller.systemBarsBehavior =
				WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
		}

		playerView.findViewById<MaterialButton>(R.id.player_fullscreen).apply {
			icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_fullscreen_exit, theme)
			setOnClickListener {
				exitFullscreen(playerView)
			}
		}
		playerView.findViewById<ViewGroup>(R.id.player_metadata).visibility = View.VISIBLE
		binding.fullscreenPlayerContainer.visibility = View.VISIBLE
		miniplayer.isDraggable = false
		requestedOrientation =
			if (isPortrait) ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
			else ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
	}

	fun exitFullscreen(playerView: View) {
		fullscreen = false
		val parentToMove = playerView.parent as View
		(parentToMove.parent as ViewGroup).removeView(parentToMove)
		findViewById<ViewGroup>(R.id.player_container).addView(parentToMove)

		WindowCompat.setDecorFitsSystemWindows(window, true)
		WindowInsetsControllerCompat(
			window,
			window.decorView
		).show(WindowInsetsCompat.Type.systemBars())

		playerView.findViewById<MaterialButton>(R.id.player_fullscreen).apply {
			icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_fullscreen, theme)
			setOnClickListener {
				enterFullscreen(playerView, player.getAspectRatio() < 1)
			}
		}
		playerView.findViewById<ViewGroup>(R.id.player_metadata).visibility = View.INVISIBLE
		binding.fullscreenPlayerContainer.visibility = View.GONE
		miniplayer.isDraggable = true
		requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_USER
	}

	private fun tryExitFullscreen(): Boolean {
		if (fullscreen) {
			exitFullscreen(findViewById(R.id.player))
			return true
		}
		return false
	}

	fun updateVideoAspectRatio(aspectRatio: Float) {
		val clampedAspectRatio = if (aspectRatio.isNaN()) 16f / 9f else aspectRatio.coerceIn(1f, 2f)
		Log.i(
			"VideoPlayer",
			"Updating player aspect ratio to $clampedAspectRatio (original: $aspectRatio)"
		)
		miniplayerScene.getConstraintSet(R.id.end)?.let {
			it.setDimensionRatio(R.id.player_container, clampedAspectRatio.toString())
			miniplayerScene.updateState(R.id.end, it)
			miniplayerScene.rebuildScene()

			// SORRY BUT I COULDNT UPDATE THE LAYOUT OTHERWISE 😭😭😭
			if (abs(miniplayerScene.progress - 1f) < 0.1f) {
				miniplayerScene.setProgress(0.999f, 10f)
				Handler(mainLooper).postDelayed({
					miniplayerScene.progress = 1f
				}, 10)
			} else if (miniplayerScene.progress < 0.1f) {
				miniplayerScene.setProgress(0.001f, 10f)
				Handler(mainLooper).postDelayed({
					miniplayerScene.progress = 0f
				}, 10)
			} else {
				// compiler doesnt shut up otherwise
			}
		}
	}
}