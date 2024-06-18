package dev.kuylar.lighttube.ui.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import dev.kuylar.lighttube.Utils
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
		binding.channelInfoSidebar.visibility =
			if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
				View.VISIBLE
			else
				View.GONE
		view.viewTreeObserver.addOnGlobalLayoutListener(object :
			ViewTreeObserver.OnGlobalLayoutListener {
			override fun onGlobalLayout() {
				view.viewTreeObserver.removeOnGlobalLayoutListener(this)
				binding.channelInfoSidebar.layoutParams = LinearLayout.LayoutParams(
					Utils.getSidebarWidth(activity as MainActivity, binding.root.width),
					LinearLayout.LayoutParams.MATCH_PARENT
				)
			}
		})
		(activity as MainActivity).apply {
			thread {
				val channel = getApi().getChannel(id, "home")
				val channelAdapter =
					ChannelAdapter(
						childFragmentManager,
						id,
						ArrayList(channel.data!!.enabledTabs.filter { it.params != "search" }.map { it.params }),
						channel
					)
				val viewPager = binding.channelPager
				runOnUiThread {
					channel.data.fillBinding(binding.header, channel.userData)
					supportActionBar?.title = channel.data.header?.title
					viewPager.adapter = channelAdapter
					binding.channelTabs.setupWithViewPager(viewPager)
				}
			}
		}
	}

	override fun onConfigurationChanged(newConfig: Configuration) {
		super.onConfigurationChanged(newConfig)

		binding.channelInfoSidebar.visibility =
			if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
				View.VISIBLE
			else
				View.GONE
	}
}