package dev.kuylar.lighttube.ui.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import dev.kuylar.lighttube.ui.fragment.RecyclerViewFragment

class ChannelAdapter(fm: FragmentManager, private val channelId: String, private val enabledTabs: ArrayList<String>) :
	FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

	override fun getCount(): Int = enabledTabs.size

	override fun getItem(i: Int): Fragment {
		val fragment = RecyclerViewFragment()
		fragment.arguments = Bundle().apply {
			putString("type", "channel")
			putString("args", channelId)
			putString("params", enabledTabs[i])
		}
		return fragment
	}

	override fun getPageTitle(position: Int): CharSequence {
		return enabledTabs[position]
	}
}
