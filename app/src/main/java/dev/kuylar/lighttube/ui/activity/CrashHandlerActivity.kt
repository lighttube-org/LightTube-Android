package dev.kuylar.lighttube.ui.activity

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dev.kuylar.lighttube.databinding.ActivityCrashHandlerBinding


class CrashHandlerActivity : AppCompatActivity() {

	private lateinit var binding: ActivityCrashHandlerBinding

	@Suppress("DEPRECATION")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityCrashHandlerBinding.inflate(layoutInflater)
		setContentView(binding.root)

		val version = try {
			val pInfo = packageManager.getPackageInfo(packageName, 0)
			"${pInfo.versionName} (build #${pInfo.versionCode})"
		} catch (e: PackageManager.NameNotFoundException) {
			null
		}

		val sp = getSharedPreferences("main", MODE_PRIVATE)
		binding.errorInstance.text = sp.getString("instanceHost", "<none>")
		binding.errorVersion.text = version
		binding.errorTrace.text = (intent.extras?.getString("trace") ?: "No stack trace provided")
	}
}