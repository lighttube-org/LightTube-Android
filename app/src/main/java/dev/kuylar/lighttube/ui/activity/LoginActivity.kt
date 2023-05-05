package dev.kuylar.lighttube.ui.activity

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.databinding.ActivityLoginBinding
import dev.kuylar.lighttube.ui.LoginWebViewClient
import kotlin.concurrent.thread


class LoginActivity : AppCompatActivity() {
	private lateinit var binding: ActivityLoginBinding
	private val scopes = listOf(
		"playlists.read",
		"playlists.write",
		"subscriptions.read",
		"subscriptions.write"
	)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityLoginBinding.inflate(layoutInflater)
		setContentView(binding.root)

		val client = LoginWebViewClient(this::onTokenReceived, this::showProgressBarLoading)
		binding.loginWebView.webViewClient = client
		binding.loginWebView.settings.databaseEnabled = true
		binding.loginWebView.loadUrl(getOauthUrl(scopes))
		Snackbar.make(binding.root, R.string.login_hint, Snackbar.LENGTH_LONG).show()
	}

	private fun showProgressBarLoading(show: Boolean) {
		if (show)
			binding.loginProgress.isIndeterminate = true
		else {
			binding.loginProgress.isIndeterminate = false
			binding.loginProgress.progress = 0
			binding.loginProgress.max = 1
		}
	}

	private fun getOauthUrl(scopes: List<String>): String {
		val sp = getSharedPreferences("main", MODE_PRIVATE)
		val host = sp.getString("instanceHost", "")
		return "$host/oauth2/authorize?response_type=code&client_id=LightTube Android&redirect_uri=lighttube://login&scope=${
			scopes.joinToString(
				" "
			)
		}"
	}

	private fun onTokenReceived(token: String) {
		showProgressBarLoading(true)
		val dialog = ProgressDialog.show(
			this,
			getString(R.string.login_token_received),
			getString(R.string.login_token_received_desc),
			true
		)
		thread {
			Thread.sleep(1000)
			runOnUiThread {
				dialog.dismiss()
				val username = "<|username|>"
				Snackbar.make(binding.root, getString(R.string.login_complete, username), Snackbar.LENGTH_LONG).show()
				startActivity(Intent(this, MainActivity::class.java))
				finish()
				showProgressBarLoading(false)
			}
		}
	}
}