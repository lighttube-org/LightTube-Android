package dev.kuylar.lighttube.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.kuylar.lighttube.api.LightTubeApi
import dev.kuylar.lighttube.api.models.SubscriptionFeedItem
import dev.kuylar.lighttube.databinding.FragmentSubscriptionsBinding
import dev.kuylar.lighttube.ui.VideoPlayerManager
import dev.kuylar.lighttube.ui.activity.MainActivity
import dev.kuylar.lighttube.ui.adapter.SubscriptionFeedRecyclerAdapter
import kotlin.concurrent.thread


class SubscriptionsFragment : Fragment() {
	private val items: MutableList<SubscriptionFeedItem> = mutableListOf()
	private lateinit var binding: FragmentSubscriptionsBinding
	private lateinit var player: VideoPlayerManager
	private lateinit var api: LightTubeApi
	private val limit = 25
	private var loading = false

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentSubscriptionsBinding.inflate(inflater)
		(activity as MainActivity).apply {
			this@SubscriptionsFragment.api = api
			this@SubscriptionsFragment.player = player
		}
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		val adapter = SubscriptionFeedRecyclerAdapter(items, this::playVideo)
		binding.recyclerFeed.layoutManager = LinearLayoutManager(context)
		binding.recyclerFeed.adapter = adapter
		binding.recyclerFeed.addOnScrollListener(object : RecyclerView.OnScrollListener() {
			override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
				super.onScrollStateChanged(recyclerView, newState)
				if (!recyclerView.canScrollVertically(1)) {
					loadMore()
				}
			}
		})
		loadMore()
	}

	private fun loadMore() {
		if (loading) return
		loading = true
		(activity as MainActivity).setLoading(true)
		thread {
			val feed = api.getSubscriptionFeed(items.size, limit)
			val start = items.size
			items.addAll(feed.data!!)
			activity?.runOnUiThread {
				(activity as MainActivity).setLoading(false)
				binding.recyclerFeed.adapter!!.notifyItemRangeInserted(start, feed.data.size)
				loading = false
			}
		}
	}

	private fun playVideo(id: String) {
		player.playVideo(id)
	}
}