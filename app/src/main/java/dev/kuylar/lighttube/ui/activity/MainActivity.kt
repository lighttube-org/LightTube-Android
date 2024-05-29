package dev.kuylar.lighttube.ui.activity

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Rect
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.Rational
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.SearchAutoComplete
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.get
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.google.android.material.elevation.SurfaceColors
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.Utils
import dev.kuylar.lighttube.api.LightTubeApi
import dev.kuylar.lighttube.api.models.LightTubeException
import dev.kuylar.lighttube.databinding.ActivityMainBinding
import dev.kuylar.lighttube.ui.AdaptiveUtils
import dev.kuylar.lighttube.ui.VideoPlayerManager
import dev.kuylar.lighttube.ui.fragment.AdaptiveFragment
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
	private lateinit var player: VideoPlayerManager
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

		val navHostFragment =
			supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
		navController = navHostFragment.navController
		// Passing each menu ID as a set of Ids because each
		// menu should be considered as top level destinations.
		val appBarConfiguration = AppBarConfiguration(
			setOf(
				R.id.navigation_home, R.id.navigation_subscriptions, R.id.navigation_library
			)
		)

		setSupportActionBar(binding.toolbar)
		setupActionBarWithNavController(navController, appBarConfiguration)
		binding.navView.setupWithNavController(navController)
		binding.navigationRail.setupWithNavController(navController)

		onBackPressedDispatcher.addCallback(this) {
			goBack(true)
		}

		val miniplayerView: View = findViewById(R.id.miniplayer)
		miniplayerScene = miniplayerView.findViewById(R.id.miniplayer_motion)
		miniplayer = BottomSheetBehavior.from(miniplayerView)
		miniplayer.state = BottomSheetBehavior.STATE_HIDDEN

		setPlayer()

		AdaptiveUtils.updateNavLayout(
			this,
			resources.configuration.screenWidthDp,
			binding.navView,
			binding.navigationRail,
			binding.navHostFragmentActivityMain,
			miniplayer
		)
		miniplayerScene.setTransition(if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) R.id.miniplayer_transition_landscape else R.id.miniplayer_transition_portrait)

		miniplayer.addBottomSheetCallback(object :
			BottomSheetBehavior.BottomSheetCallback() {
			@SuppressLint("SwitchIntDef")
			override fun onStateChanged(bottomSheet: View, newState: Int) {
				when (newState) {
					BottomSheetBehavior.STATE_HIDDEN -> getPlayer().stop()
				}
			}

			override fun onSlide(bottomSheet: View, slideOffset: Float) {
				val p = getPlayer()
				if (slideOffset < 0)
					p.setVolume(max(0.1f, 1 + slideOffset * 3))
				else
					p.setVolume(1f)
				miniplayerScene.progress = max(0f, min(1f, slideOffset * 5))

				AdaptiveUtils.toggleNavBars(
					this@MainActivity,
					slideOffset > .3,
					resources.configuration.screenWidthDp,
					binding.navView,
					binding.navigationRail
				)
				binding.appBarLayout.visibility =
					if (slideOffset > .95) View.GONE else if (slideOffset < .8) View.VISIBLE else View.GONE
				p.toggleControls(slideOffset * 5 >= 1)
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

		// set current user
		thread {
			updateCurrentUser()
		}

		val handler = Handler(mainLooper)
		var sponsorblockRunnable = Runnable {}
		sponsorblockRunnable = Runnable {
			try {
				with(getPlayer()) {
					updateSkipButton(getCurrentSegment())
				}
			} catch (e: Exception) {
				Log.e("SponsorBlockLoop", "Failed to update SponsorBlock skip button", e)
			}
			handler.postDelayed(sponsorblockRunnable, 100)
		}
		handler.postDelayed(sponsorblockRunnable, 100)
		ContextCompat.registerReceiver(this, object : BroadcastReceiver() {
			override fun onReceive(context: Context?, intent: Intent?) {
				onBroadcastReceived(context, intent)
			}
		}, IntentFilter(ACTION_PLAY_PAUSE), ContextCompat.RECEIVER_EXPORTED)

		if (intent != null)
			handleDeepLinks(intent.action, intent.data)
	}

	override fun onConfigurationChanged(newConfig: Configuration) {
		super.onConfigurationChanged(newConfig)

		if (isInPictureInPictureMode) return

		AdaptiveUtils.updateNavLayout(
			this,
			newConfig.screenWidthDp,
			binding.navView,
			binding.navigationRail,
			binding.navHostFragmentActivityMain,
			miniplayer
		)
		miniplayerScene.setTransition(if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) R.id.miniplayer_transition_landscape else R.id.miniplayer_transition_portrait)
		miniplayerScene.progress = miniplayerScene.progress
		if (this::player.isInitialized)
			player.notifyScreenRotated(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)

		binding.navHostFragmentActivityMain.getFragment<NavHostFragment>().childFragmentManager.fragments.forEach {
			if (it is AdaptiveFragment)
				it.onScreenSizeChanged(newConfig.screenWidthDp)
		}
	}

	private fun handleDeepLinks(action: String?, data: Uri?): Boolean {
		if (action == null || data == null) return false
		val path = if (data.path == "/attribution_link") Utils.unwrapAttributionUrl(
			data.query ?: ""
		) else ((data.path ?: "/") + "?" + (data.query ?: "")).trimEnd('?')
		val query =
			if (path.contains('?')) Utils.parseQueryString(path.split("?")[1]) else HashMap()

		fun video(id: String, time: String?, playlist: String?) {
			player.playVideo(id) //todo: time, playlist
			miniplayerScene.progress = 1f
			binding.navView.visibility = View.GONE
		}

		fun channel(id: String, tab: String?) {
			val realTab = if (tab == "featured" || tab == null) "home" else tab
			navController.navigate(
				R.id.navigation_channel,
				bundleOf(Pair("id", id), Pair("tab", realTab))
			)
		}

		fun playlist(id: String) {
			navController.navigate(R.id.navigation_playlist, bundleOf(Pair("id", id)))
		}

		return try {
			if (path.startsWith("/watch")) {
				video(query["v"]!!, query["t"], query["list"])
			} else if (path.startsWith("/v/")) {
				video(path.split('/')[2].split('?')[0], query["t"], query["list"])
			} else if (path.startsWith("/embed/") || path.startsWith("/shorts/") || path.startsWith(
					"/live/"
				)
			) {
				video(path.split('/')[2].split('?')[0], query["t"] ?: query["start"], query["list"])
			} else if (path.startsWith("/channel/") || path.startsWith("/user/") || path.startsWith(
					"/c/"
				)
			) {
				val parts = path.split('/')
				channel(
					parts[2].split('?')[0],
					if (parts.size > 3) parts[3].split('?')[0] else null
				)
			} else if (path.startsWith("/@")) {
				val parts = path.split('/')
				channel(
					parts[1].split('?')[0],
					if (parts.size > 2) parts[2].split('?')[0] else null
				)
			} else if (path.startsWith("/playlist")) {
				playlist(query["list"]!!)
			} else if (data.host == "youtu.be") {
				video(path.trimStart('/').split('?')[0], query["t"], query["list"])
			} else {
				throw IllegalArgumentException()
			}
			true
		} catch (e: Exception) {
			Toast.makeText(this, R.string.error_intent_filter, Toast.LENGTH_LONG).show()
			false
		}
	}

	override fun onNewIntent(intent: Intent?) {
		var success = false
		if (intent != null)
			success = handleDeepLinks(intent.action, intent.data)

		if (!success)
			super.onNewIntent(intent)
	}

	private fun goBack(closeApp: Boolean): Boolean {
		if (!getPlayer().closeSheets()) // attempt to close details/comments
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

	private fun setPlayer() {
		player = VideoPlayerManager(this)
	}

	fun getPlayer(): VideoPlayerManager {
		if (!this::player.isInitialized)
			setPlayer()
		return player
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
				enterFullscreen(playerView, getPlayer().getAspectRatio() < 1)
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
		val clampedAspectRatio =
			if (aspectRatio.isNaN()) 16f / 9f else aspectRatio.coerceIn(1f, 2f)
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
		try {
			setPictureInPictureParams(getPipParams())
		} catch (_: IllegalAccessException) {
			// ignored, the aspect ratio is bad
		}
	}

	private fun updateCurrentUser(retryCount: Int = 0) {
		try {
			val user = api.getCurrentUser().data
			if (user != null)
				api.currentUser = user
		} catch (_: Exception) {
			if (retryCount < 5)
				updateCurrentUser(retryCount + 1)
		}
	}

	private fun getPipParams(): PictureInPictureParams {
		val playerContainer =
			if (fullscreen) findViewById<View>(R.id.player_container) else findViewById(R.id.player_container)
		val rect = Rect()
		playerContainer.getGlobalVisibleRect(rect)

		return PictureInPictureParams.Builder().apply {
			this.setAspectRatio(Rational(rect.width(), rect.height()))
			this.setSourceRectHint(rect)
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
				this.setAutoEnterEnabled(player.isPlaying() && miniplayer.state == BottomSheetBehavior.STATE_EXPANDED)
			}
			this.setActions(
				if (player.isPlaying()) {
					listOf(
						RemoteAction(
							Icon.createWithResource(this@MainActivity, R.drawable.ic_pause),
							getString(R.string.pause),
							getString(R.string.pause),
							PendingIntent.getBroadcast(
								this@MainActivity,
								REQUEST_CODE_PAUSE,
								Intent(ACTION_PLAY_PAUSE).putExtra(
									"requestCode",
									REQUEST_CODE_PAUSE
								),
								PendingIntent.FLAG_IMMUTABLE
							)
						)
					)
				} else {
					listOf(
						RemoteAction(
							Icon.createWithResource(applicationContext, R.drawable.ic_play),
							getString(R.string.play),
							getString(R.string.play),
							PendingIntent.getBroadcast(
								this@MainActivity,
								REQUEST_CODE_PLAY,
								Intent(ACTION_PLAY_PAUSE).putExtra(
									"requestCode",
									REQUEST_CODE_PLAY
								),
								PendingIntent.FLAG_IMMUTABLE
							)
						)
					)
				}
			)
		}.build()
	}

	override fun onPictureInPictureModeChanged(
		isInPictureInPictureMode: Boolean,
		newConfig: Configuration
	) {
		if (lifecycle.currentState == Lifecycle.State.CREATED) {
			player.pause()
		}
		super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
		player.toggleControls(!isInPictureInPictureMode)
	}

	fun enterPip() {
		enterPictureInPictureMode(getPipParams())
	}

	fun updatePlaying() {
		setPictureInPictureParams(getPipParams())
	}

	fun canPip() = packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)

	private fun onBroadcastReceived(context: Context?, intent: Intent?) {
		if (intent == null) return
		if (intent.action == ACTION_PLAY_PAUSE) {
			if (intent.getIntExtra("requestCode", REQUEST_CODE_PLAY) == REQUEST_CODE_PAUSE)
				player.pause()
			else
				player.play()
		}
	}

	companion object {
		const val ACTION_PLAY_PAUSE = "MainActivity.Pip.PlayPauseAction"
		const val REQUEST_CODE_PLAY = 0
		const val REQUEST_CODE_PAUSE = 1
	}
}