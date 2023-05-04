package dev.kuylar.lighttube.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.elevation.SurfaceColors
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.api.InstanceListApi
import dev.kuylar.lighttube.databinding.ActivitySetupBinding
import kotlin.concurrent.thread

class SetupActivity : AppCompatActivity() {
	private lateinit var binding: ActivitySetupBinding
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val sp = getSharedPreferences("main", MODE_PRIVATE)

		// set status bar & bottom nav bar colors
		val color = SurfaceColors.SURFACE_2.getColor(this)
		window.statusBarColor = color
		window.navigationBarColor = color

		binding = ActivitySetupBinding.inflate(layoutInflater)
		setContentView(binding.root)

		thread {
			val instances = InstanceListApi.getInstances()
			runOnUiThread {
				while (binding.instanceList.childCount > 0) {
					binding.instanceList.removeViewAt(0)
				}

				for (instance in instances) {
					if (!instance.api) continue

					val v = layoutInflater.inflate(R.layout.item_instance, null)
					v.findViewById<TextView>(R.id.instanceTitle).text = instance.host
					v.findViewById<TextView>(R.id.instanceDescription).text = getString(
						R.string.template_instance_info,
						if (instance.api) getString(R.string.enabled) else getString(R.string.disabled),
						if (instance.accounts) getString(R.string.enabled) else getString(R.string.disabled)
					)
					v.setOnClickListener {
						sp.edit().apply {
							putString("instanceHost", instance.host)
							apply()
						}
						if (!instance.accounts) finishSetup()
						else {
							binding.setupScreenInstance.visibility = View.GONE
							binding.setupScreenLogin.visibility = View.VISIBLE
						}
					}
					binding.instanceList.addView(v)
				}
			}
		}

		binding.setupButtonWelcomeNext.setOnClickListener {
			binding.setupScreenWelcome.visibility = View.GONE
			binding.setupScreenInstance.visibility = View.VISIBLE
		}

		binding.setupButtonLoginSkip.setOnClickListener {
			finishSetup()
		}
	}

	private fun finishSetup() {
		startActivity(Intent(this, MainActivity::class.java))
		finish()
	}
}