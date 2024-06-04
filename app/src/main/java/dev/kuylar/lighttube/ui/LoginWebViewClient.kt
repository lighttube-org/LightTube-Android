package dev.kuylar.lighttube.ui

import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient


class LoginWebViewClient(
	private val onTokenReceived: (String) -> Unit,
	private val showLoading: (Boolean) -> Unit
) : WebViewClient() {
	override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
		if (request == null) return false;
		return if (request.url != null && request.url.scheme == "lighttube") {
			if (request.url.host == "login" && request.url.queryParameterNames.contains("code"))
				onTokenReceived.invoke(request.url.getQueryParameter("code")!!)
			true
		} else {
			false
		}
	}

	override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
		showLoading.invoke(true)
	}

	override fun onPageFinished(view: WebView?, url: String?) {
		showLoading.invoke(false)
	}


}