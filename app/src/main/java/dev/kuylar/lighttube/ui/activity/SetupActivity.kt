package dev.kuylar.lighttube.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.elevation.SurfaceColors
import dev.kuylar.lighttube.databinding.ActivitySetupBinding
import dev.kuylar.lighttube.ui.fragment.setup.SetupCustomInstanceFragment
import dev.kuylar.lighttube.ui.fragment.setup.SetupFinishFragment
import dev.kuylar.lighttube.ui.fragment.setup.SetupInstanceSelectFragment
import dev.kuylar.lighttube.ui.fragment.setup.SetupLoginFragment
import dev.kuylar.lighttube.ui.fragment.setup.SetupWelcomeFragment

class SetupActivity : AppCompatActivity() {
	private lateinit var binding: ActivitySetupBinding
	private var publicInstancesFragment: Fragment? = null
	private var customInstanceFragment: Fragment? = null


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		getSharedPreferences("main", MODE_PRIVATE)

		// set status bar & bottom nav bar colors
		val color = SurfaceColors.SURFACE_2.getColor(this)
		window.statusBarColor = color
		window.navigationBarColor = color

		binding = ActivitySetupBinding.inflate(layoutInflater)
		setContentView(binding.root)
	}

	fun finishSetup() {
		startActivity(Intent(this, MainActivity::class.java))
		finish()
	}

	fun goToStage(stage: Int) {
		if (!validStages.contains(stage)) {
			Toast.makeText(this, "invalid setup stage: $stage", Toast.LENGTH_LONG).show()
			return
		}
		val fragment = when (stage) {
			STAGE_WELCOME -> SetupWelcomeFragment()
			STAGE_INSTANCES_PUBLIC -> {
				if (publicInstancesFragment == null)
					publicInstancesFragment = SetupInstanceSelectFragment()
				publicInstancesFragment!!
			}

			STAGE_INSTANCES_CUSTOM -> {
				if (customInstanceFragment == null)
					customInstanceFragment = SetupCustomInstanceFragment()
				customInstanceFragment!!
			}

			STAGE_LOGIN -> SetupLoginFragment()
			STAGE_FINISH -> SetupFinishFragment()
			else -> SetupWelcomeFragment()
		}
		supportFragmentManager.beginTransaction().apply {
			replace(binding.setupFragmentContainer.id, fragment)
		}.commit()
	}

	companion object {
		const val STAGE_WELCOME = 0
		const val STAGE_INSTANCES_PUBLIC = 1
		const val STAGE_INSTANCES_CUSTOM = 2
		const val STAGE_LOGIN = 3
		const val STAGE_FINISH = 4
		const val REQUEST_CODE_LOGIN = 0
		const val RESULT_CODE_LOGIN_SUCCESS = 0
		const val RESULT_CODE_LOGIN_FAIL = 1

		val validStages = arrayOf(
			STAGE_WELCOME,
			STAGE_INSTANCES_PUBLIC,
			STAGE_INSTANCES_CUSTOM,
			STAGE_LOGIN,
			STAGE_FINISH,
		)
	}
}