package dev.kuylar.lighttube.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.elevation.SurfaceColors
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

	private lateinit var binding: ActivityMainBinding

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
		}

		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)

		val navView: BottomNavigationView = binding.navView
		val navController = findNavController(R.id.nav_host_fragment_activity_main)
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
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.top_app_bar, menu)
		return true
	}
}