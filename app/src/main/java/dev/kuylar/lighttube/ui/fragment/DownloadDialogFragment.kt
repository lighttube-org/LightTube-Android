package dev.kuylar.lighttube.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.media3.common.util.UnstableApi
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.api.models.Format
import dev.kuylar.lighttube.api.models.LightTubeVideo
import dev.kuylar.lighttube.databinding.FragmentDownloadDialogBinding
import dev.kuylar.lighttube.downloads.DownloadInfo
import dev.kuylar.lighttube.downloads.VideoDownloadManager
import dev.kuylar.lighttube.ui.activity.MainActivity
import kotlin.concurrent.thread

@UnstableApi
class DownloadDialogFragment(private val video: LightTubeVideo) : BottomSheetDialogFragment() {
	private lateinit var binding: FragmentDownloadDialogBinding
	private var formats: List<Format>? = null

	private var has22: Boolean? = null
	private var instanceAllowsThirdPartyProxy: Boolean? = null

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
	): View {
		binding = FragmentDownloadDialogBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		thread {
			val api = (activity as MainActivity).getApi()
			val instanceInfo = api.getInstanceInfo()
			formats = api.getPlayer(video.id).data?.formats

			activity?.runOnUiThread {
				try {
					if (formats.isNullOrEmpty()) {
						Toast.makeText(
							requireContext(),
							R.string.error_download_no_formats,
							Toast.LENGTH_LONG
						).show()
						dismiss()
						return@runOnUiThread
					}
					has22 = formats!!.any { it.itag == "22" }
					instanceAllowsThirdPartyProxy = instanceInfo.allowsThirdPartyProxyUsage
					toggleUi(true)
				} catch (_: Exception) {
				}
			}
		}
	}

	private fun toggleUi(enabled: Boolean) {
		binding.itag18.isEnabled = enabled
		binding.itag22.isEnabled = has22 == true && enabled
		binding.switchProxy.isEnabled = instanceAllowsThirdPartyProxy == true && enabled
		if (instanceAllowsThirdPartyProxy == false)
			binding.switchProxy.isChecked = false
		binding.buttonDownload.isEnabled = enabled
		binding.buttonDownload.setOnClickListener {
			if (enabled) doDownload()
		}

		binding.loadingBar.visibility = if (enabled) View.GONE else View.VISIBLE
	}

	private fun doDownload() {
		val useProxy = binding.switchProxy.isChecked
		val itag = when (binding.downloadItag.checkedRadioButtonId) {
			R.id.itag_18 -> "18"
			R.id.itag_22 -> "22"
			else -> "18"
		}

		toggleUi(false)
		thread {
			try {
				VideoDownloadManager.startDownload(
					requireContext(),
					video.id,
					formats!!.first { it.itag == itag },
					DownloadInfo(
						video.id,
						video.title,
						video.description,
						video.viewCount,
						video.dateText,
						video.channel.id,
						video.channel.avatar!!,
						video.channel.title,
						video.channel.subscribers!!,
						video.likeCount
					),
					useProxy
				)
				activity?.runOnUiThread {
					dismiss()
				}
			} catch (e: Exception) {
				activity?.runOnUiThread {
					Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
					toggleUi(true)
				}
			}
		}
	}
}