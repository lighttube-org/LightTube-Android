package dev.kuylar.lighttube.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.JsonObject
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.api.LightTubeApi
import dev.kuylar.lighttube.api.models.LightTubeException
import dev.kuylar.lighttube.databinding.FragmentSearchBinding
import dev.kuylar.lighttube.ui.VideoPlayerManager
import dev.kuylar.lighttube.ui.activity.MainActivity
import dev.kuylar.lighttube.ui.adapter.RendererRecyclerAdapter
import java.io.IOException
import kotlin.concurrent.thread


class SearchFragment : Fragment() {
	private val items: MutableList<JsonObject> = mutableListOf()
	private lateinit var binding: FragmentSearchBinding
	private lateinit var player: VideoPlayerManager
	private lateinit var api: LightTubeApi
	private lateinit var query: String
	private var loading = false
	private var contKey: String? = null

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentSearchBinding.inflate(inflater)
		(activity as MainActivity).apply {
			this@SearchFragment.api = getApi()
			this@SearchFragment.player = player
		}
		query = arguments?.getString("query") ?: "asdf"
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		val adapter = RendererRecyclerAdapter(items)
		binding.recyclerSearch.layoutManager = LinearLayoutManager(context)
		binding.recyclerSearch.adapter = adapter
		binding.recyclerSearch.itemAnimator = null
		binding.recyclerSearch.addOnScrollListener(object : RecyclerView.OnScrollListener() {
			override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
				super.onScrollStateChanged(recyclerView, newState)
				if (!recyclerView.canScrollVertically(1)) {
					loadMore(false)
				}
			}
		})
		loadMore(true)
	}

	private fun loadMore(initial: Boolean) {
		if (loading) return
		if (!initial && contKey == null) return
		loading = true
		(activity as MainActivity).setLoading(true)
		thread {
			try {
				val feed = if (initial) api.search(query) else api.continueSearch(contKey!!)
				val start = items.size
				items.addAll(feed.data!!.searchResults)
				contKey = feed.data.continuationKey
				if (activity == null) return@thread
				activity?.runOnUiThread {
					(activity as MainActivity).setLoading(false)
					(binding.recyclerSearch.adapter!! as RendererRecyclerAdapter).updateUserData(feed.userData)
					binding.recyclerSearch.adapter!!.notifyItemRangeInserted(
						start,
						feed.data.searchResults.size
					)
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
						loadMore(initial)
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
						loadMore(initial)
						sb.dismiss()
					}
					sb.show()
				}
			}
		}
	}
}