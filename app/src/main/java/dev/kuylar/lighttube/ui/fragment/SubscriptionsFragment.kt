package dev.kuylar.lighttube.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.api.LightTubeApi
import dev.kuylar.lighttube.api.models.LightTubeException
import dev.kuylar.lighttube.api.models.SubscriptionFeedItem
import dev.kuylar.lighttube.databinding.FragmentSubscriptionsBinding
import dev.kuylar.lighttube.ui.AdaptiveUtils
import dev.kuylar.lighttube.ui.VideoPlayerManager
import dev.kuylar.lighttube.ui.activity.MainActivity
import dev.kuylar.lighttube.ui.adapter.SubscriptionFeedRecyclerAdapter
import java.io.IOException
import kotlin.concurrent.thread


class SubscriptionsFragment : Fragment(), AdaptiveFragment {
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
			this@SubscriptionsFragment.api = getApi()
			this@SubscriptionsFragment.player = getPlayer()
		}
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		val adapter = SubscriptionFeedRecyclerAdapter(items, this::playVideo)
		binding.recyclerFeed.adapter = adapter
		binding.recyclerFeed.addOnScrollListener(object : RecyclerView.OnScrollListener() {
			override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
				super.onScrollStateChanged(recyclerView, newState)
				if (!recyclerView.canScrollVertically(1)) {
					loadMore()
				}
			}
		})
		view.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
			override fun onGlobalLayout() {
				view.viewTreeObserver.removeOnGlobalLayoutListener(this)
				binding.recyclerFeed.layoutManager = GridLayoutManager(
					context,
					AdaptiveUtils.getColumnCount(
						binding.recyclerFeed.width / requireContext().resources.displayMetrics.density,
						AdaptiveUtils.ITEM_TYPE_VIDEO
					)
				)
			}
		})
		loadMore()
	}

	private fun loadMore() {
		if (loading) return
		loading = true
		if (activity == null) return
		(activity as MainActivity).setLoading(true)
		thread {
			try {
				val feed = api.getSubscriptionFeed(items.size, limit)
				if (activity == null) return@thread
				val start = items.size
				items.addAll(feed.data!!)
				activity?.runOnUiThread {
					(activity as MainActivity).setLoading(false)
					binding.recyclerFeed.adapter!!.notifyItemRangeInserted(start, feed.data.size)
					loading = false
				}
			} catch (e: IOException) {
				if (activity == null) return@thread
				activity?.runOnUiThread {
					loading = false
					(activity as MainActivity).setLoading(false)
					val sb = Snackbar.make(
						binding.root,
						R.string.error_connection,
						Snackbar.LENGTH_INDEFINITE
					)
					sb.setAnchorView(R.id.nav_view)
					sb.setAction(R.string.action_retry) {
						loadMore()
						sb.dismiss()
					}
					sb.show()
				}
			} catch (e: LightTubeException) {
				if (activity == null) return@thread
				activity?.runOnUiThread {
					loading = false
					(activity as MainActivity).setLoading(false)
					val sb = Snackbar.make(
						binding.root,
						getString(R.string.error_lighttube, e.message),
						Snackbar.LENGTH_INDEFINITE
					)
					sb.setTextMaxLines(2)
					sb.setAnchorView(R.id.nav_view)
					sb.setAction(R.string.action_retry) {
						loadMore()
						sb.dismiss()
					}
					sb.show()
				}
			}
		}
	}

	private fun playVideo(id: String) {
		player.playVideo(id)
	}

	override fun onScreenSizeChanged(newSize: Int) {
		binding.recyclerFeed.layoutManager = GridLayoutManager(
			context,
			AdaptiveUtils.getColumnCount(
				binding.recyclerFeed.width / requireContext().resources.displayMetrics.density,
				AdaptiveUtils.ITEM_TYPE_VIDEO
			)
		)
	}
}