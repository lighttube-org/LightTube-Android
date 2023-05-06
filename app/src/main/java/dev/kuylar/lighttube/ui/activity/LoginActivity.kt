package dev.kuylar.lighttube.ui.activity

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.api.LightTubeApi
import dev.kuylar.lighttube.api.UtilityApi
import dev.kuylar.lighttube.databinding.ActivityLoginBinding
import dev.kuylar.lighttube.ui.LoginWebViewClient
import kotlin.concurrent.thread


class LoginActivity : AppCompatActivity() {
	private lateinit var sp: SharedPreferences
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

		sp = getSharedPreferences("main", MODE_PRIVATE)

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
			UtilityApi.authorizeToken(
				sp.getString("instanceHost", "")!!,
				token,
				"lighttube://login"
			)
			sp.edit().apply {
				putString("refreshToken", token)
				apply()
			}
			val api = LightTubeApi(this)
			val user = api.getCurrentUser()
			val username = user.data!!.userID
			runOnUiThread {
				dialog.dismiss()
				Toast.makeText(
					this,
					getString(R.string.login_complete, username),
					Toast.LENGTH_LONG
				).show()
				showProgressBarLoading(false)
				startActivity(Intent(this, MainActivity::class.java))
				finish()
			}
		}
	}
}