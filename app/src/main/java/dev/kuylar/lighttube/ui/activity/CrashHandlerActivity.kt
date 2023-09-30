package dev.kuylar.lighttube.ui.activity

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dev.kuylar.lighttube.BuildConfig
import dev.kuylar.lighttube.databinding.ActivityCrashHandlerBinding


@SuppressLint("SetTextI18n")
// todo: make this translatable
// or make it so this is never shown to the user, both works
class CrashHandlerActivity : AppCompatActivity() {

	private lateinit var binding: ActivityCrashHandlerBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityCrashHandlerBinding.inflate(layoutInflater)
		setContentView(binding.root)

		val sp = getSharedPreferences("main", MODE_PRIVATE)
		binding.errorInstance.text = sp.getString("instanceHost", "<none>")
		binding.errorVersion.text = "${BuildConfig.VERSION_NAME} (build #${BuildConfig.VERSION_CODE})"
		binding.errorTrace.text = (intent.extras?.getString("trace") ?: "No stack trace provided")
		binding.errorTrace.setHorizontallyScrolling(true)
	}
}