package dev.kuylar.lighttube.ui.activity

import android.app.ProgressDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.google.android.material.snackbar.Snackbar
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.api.LightTubeApi
import dev.kuylar.lighttube.api.UtilityApi
import dev.kuylar.lighttube.databinding.ActivityLoginBinding
import dev.kuylar.lighttube.ui.LoginWebViewClient
import java.net.URLEncoder
import java.nio.charset.Charset
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

		val isRegister = intent.extras?.getBoolean("register", false) ?: false

		sp = getSharedPreferences("main", MODE_PRIVATE)

		val client = LoginWebViewClient(this::onTokenReceived, this::showProgressBarLoading)
		binding.loginWebView.webViewClient = client
		binding.loginWebView.settings.databaseEnabled = true
		binding.loginWebView.loadUrl(if (isRegister) getRegisterUrl(scopes) else getOauthUrl(scopes))
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

	private fun getRegisterUrl(scopes: List<String>): String {
		val sp = getSharedPreferences("main", MODE_PRIVATE)
		val host = sp.getString("instanceHost", "")
		val redirectUrl =
			"/oauth2/authorize?response_type=code&client_id=LightTube Android&redirect_uri=lighttube://login&scope=${
				scopes.joinToString(
					" "
				)
			}"
		return "$host/account/register?redirectUrl=${
			URLEncoder.encode(
				redirectUrl,
				Charset.defaultCharset().name()
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
			val api = LightTubeApi(this, token)
			val user = api.getCurrentUser()
			val username = user.data!!.userID
			sp.edit {
				putString("refreshToken", token)
				putString("username", username)
			}
			runOnUiThread {
				dialog.dismiss()
				Toast.makeText(
					this,
					getString(R.string.login_complete, username),
					Toast.LENGTH_LONG
				).show()
				showProgressBarLoading(false)
				setResult(RESULT_OK)
				finish()
			}
		}
	}
}