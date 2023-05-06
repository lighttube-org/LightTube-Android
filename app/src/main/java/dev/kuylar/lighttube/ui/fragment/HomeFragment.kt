package dev.kuylar.lighttube.ui.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import dev.kuylar.lighttube.databinding.FragmentHomeBinding
import dev.kuylar.lighttube.ui.activity.MainActivity
import kotlin.concurrent.thread

class HomeFragment : Fragment() {
	private lateinit var binding: FragmentHomeBinding

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentHomeBinding.inflate(inflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		val a = (activity as MainActivity)
		val sp = a.getSharedPreferences("main", Activity.MODE_PRIVATE)
		binding.homeMotd.text = sp.getString("cachedMotd", "Search something to get started!")

		thread {
			val info = a.api.getInstanceInfo()
			a.runOnUiThread {
				binding.homeMotd.text = info.motd
				sp.edit {
					putString("cachedMotd", info.motd)
				}
			}
		}
	}
}