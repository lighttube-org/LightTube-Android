package dev.kuylar.lighttube.ui.fragment.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import dev.kuylar.lighttube.api.UtilityApi
import dev.kuylar.lighttube.databinding.FragmentSetupInstanceSelectBinding
import dev.kuylar.lighttube.ui.activity.SetupActivity
import dev.kuylar.lighttube.ui.adapter.InstanceListAdapter
import kotlin.concurrent.thread

class SetupInstanceSelectFragment : Fragment() {
	private lateinit var binding: FragmentSetupInstanceSelectBinding

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentSetupInstanceSelectBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		binding.custom.setOnClickListener {
			(activity as SetupActivity).goToStage(SetupActivity.STAGE_INSTANCES_CUSTOM)
		}
		binding.recyclerInstances.layoutManager =
			LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

		thread {
			val instances = UtilityApi.getInstances()
			activity?.runOnUiThread {
				binding.loading.visibility = View.GONE
				binding.recyclerInstances.adapter =
					InstanceListAdapter(requireActivity(), instances.shuffled()) {
						next(it.scheme, it.host, it.accountsEnabled)
					}
			}
		}
	}

	fun next(scheme: String, host: String, accountsEnabled: Boolean) {
		val sp = requireContext().getSharedPreferences("main", AppCompatActivity.MODE_PRIVATE)
		sp.edit {
			putString("instanceHost", "$scheme://$host")
			putBoolean("customInstance", false)
		}
		(activity as SetupActivity).goToStage(if (accountsEnabled) SetupActivity.STAGE_LOGIN else SetupActivity.STAGE_FINISH)
	}
}