package dev.kuylar.lighttube.ui.fragment.setup

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.api.LightTubeApi
import dev.kuylar.lighttube.api.models.InstanceInfo
import dev.kuylar.lighttube.databinding.FragmentSetupCustomInstanceBinding
import dev.kuylar.lighttube.ui.activity.SetupActivity
import kotlin.concurrent.thread

class SetupCustomInstanceFragment : Fragment() {
	private lateinit var binding: FragmentSetupCustomInstanceBinding
	private var loadingUrl = ""

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentSetupCustomInstanceBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		binding.publicInstances.setOnClickListener {
			(activity as SetupActivity).goToStage(SetupActivity.STAGE_INSTANCES_PUBLIC)
		}
		binding.fieldInstance.editText!!.setOnKeyListener { _, _, _ ->
			updateInstanceInfo(binding.fieldInstance.editText!!.editableText.toString())
			false
		}
	}

	private fun updateInstanceInfo(url: String) {
		if (url == loadingUrl) return
		loadingUrl = url
		clearCard()
		thread {
			try {
				val instanceInfo = LightTubeApi(url).getInstanceInfo()
				activity?.runOnUiThread {
					if (loadingUrl != url) return@runOnUiThread
					fillData(instanceInfo, url)
				}
			} catch (e: Exception) {
				activity?.runOnUiThread {
					if (loadingUrl != url) return@runOnUiThread
					fillError(e, url)
				}
			}
		}
	}

	private fun fillData(instanceInfo: InstanceInfo, url: String) {
		binding.instanceCard.loading.visibility = View.GONE
		binding.instanceCard.instanceTitle.text = url
		binding.instanceCard.instanceDescription.text = if (instanceInfo.type != "lighttube/2.0")
			getString(R.string.setup_instance_invalid, instanceInfo.type)
		else if (instanceInfo.config.allowsApi) {
			binding.instanceCard.root.setOnClickListener {
				next(url, instanceInfo.config.allowsOauthApi)
			}
			binding.instanceCard.instanceCloudflare.visibility = View.VISIBLE
			binding.instanceCard.instanceCloudflare.setText(R.string.setup_instance_custom_select)
			getString(
				R.string.template_instance_info,
				instanceInfo.version,
				if (instanceInfo.config.allowsOauthApi) getString(R.string.enabled) else getString(R.string.disabled),
				if (instanceInfo.config.allowsThirdPartyProxyUsage) getString(R.string.enabled) else getString(
					R.string.disabled)
			)
		}
		else getString(R.string.setup_instance_api_disabled)
	}

	@SuppressLint("SetTextI18n")
	private fun fillError(e: Exception, url: String) {
		clearCard()
		binding.instanceCard.loading.visibility = View.GONE
		binding.instanceCard.instanceTitle.text = getString(R.string.setup_instance_load_fail, url)
		binding.instanceCard.instanceDescription.text = "${e.javaClass.name}: ${e.message}"
	}

	private fun clearCard() {
		binding.instanceCard.root.setOnClickListener {  }
		binding.instanceCard.loading.visibility = View.VISIBLE
		binding.instanceCard.instanceTitle.text = ""
		binding.instanceCard.instanceDescription.text = ""
		binding.instanceCard.instanceCloudflare.visibility = View.GONE
	}

	private fun next(url: String, accountsEnabled: Boolean) {
		val sp = requireContext().getSharedPreferences("main", AppCompatActivity.MODE_PRIVATE)
		sp.edit {
			putString("instanceHost", url)
			putBoolean("customInstance", true)
		}
		(activity as SetupActivity).goToStage(if (accountsEnabled) SetupActivity.STAGE_LOGIN else SetupActivity.STAGE_FINISH)
	}
}