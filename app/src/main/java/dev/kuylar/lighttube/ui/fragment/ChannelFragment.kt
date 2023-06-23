package dev.kuylar.lighttube.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dev.kuylar.lighttube.databinding.FragmentChannelBinding
import dev.kuylar.lighttube.ui.activity.MainActivity
import dev.kuylar.lighttube.ui.adapter.ChannelAdapter
import kotlin.concurrent.thread

class ChannelFragment : Fragment() {
	private lateinit var id: String
	private lateinit var initialTab: String
	private lateinit var binding: FragmentChannelBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		arguments?.let {
			id = it.getString("id")!!
			initialTab = it.getString("tab") ?: "home"
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentChannelBinding.inflate(inflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		(activity as MainActivity).apply {
			thread {
				val channel = api.getChannel(id)
				val channelAdapter =
					ChannelAdapter(childFragmentManager, id, ArrayList(channel.data!!.enabledTabs.filter { it.lowercase() != "search" }))
				val viewPager = binding.channelPager
				runOnUiThread {
					supportActionBar?.title = channel.data.title
					viewPager.adapter = channelAdapter
					binding.channelTabs.setupWithViewPager(viewPager)
				}
			}
		}
	}
}