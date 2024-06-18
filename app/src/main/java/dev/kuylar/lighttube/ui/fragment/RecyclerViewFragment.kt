package dev.kuylar.lighttube.ui.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.Utils
import dev.kuylar.lighttube.api.LightTubeApi
import dev.kuylar.lighttube.api.models.ApiResponse
import dev.kuylar.lighttube.api.models.LightTubeChannel
import dev.kuylar.lighttube.api.models.LightTubeException
import dev.kuylar.lighttube.api.models.UserData
import dev.kuylar.lighttube.api.models.renderers.ContinuationRendererData
import dev.kuylar.lighttube.api.models.renderers.RendererContainer
import dev.kuylar.lighttube.databinding.FragmentRecyclerviewBinding
import dev.kuylar.lighttube.ui.VideoPlayerManager
import dev.kuylar.lighttube.ui.activity.MainActivity
import dev.kuylar.lighttube.ui.adapter.RendererRecyclerAdapter
import java.io.IOException
import kotlin.concurrent.thread

class RecyclerViewFragment : Fragment(), AdaptiveFragment {
	private val items: MutableList<RendererContainer> = mutableListOf()
	private var userData: UserData? = null
	private lateinit var binding: FragmentRecyclerviewBinding
	private lateinit var player: VideoPlayerManager
	private lateinit var api: LightTubeApi
	private lateinit var type: String
	private lateinit var args: String
	private var initialData: String? = null
	private var params: String? = null
	private var contKey: String? = null
	private var loading = false

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentRecyclerviewBinding.inflate(inflater)
		type = arguments?.getString("type")!!
		args = arguments?.getString("args")!!
		params = arguments?.getString("params")
		initialData = arguments?.getString("initialData")
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		(activity as MainActivity).apply {
			this@RecyclerViewFragment.api = getApi()
			this@RecyclerViewFragment.player = getPlayer()
		}
		val adapter = RendererRecyclerAdapter(items)
		adapter.notifyScreenRotated(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
		binding.recycler.layoutManager = LinearLayoutManager(context)
		binding.recycler.adapter = adapter
		binding.recycler.itemAnimator = null
		binding.recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
		(activity as MainActivity).apply {
			setLoading(true)
			thread {
				try {
					val (newItems, continuation) = getData(initial)
					contKey = continuation
					runOnUiThread {
						setLoading(false)
						items.removeIf { it.type == "continuation" }
						binding.recycler.adapter!!.notifyItemRemoved(items.size)

						val start = items.size
						items.addAll(newItems.first)
						(binding.recycler.adapter!! as RendererRecyclerAdapter).updateUserData(newItems.second)
						binding.recycler.adapter!!.notifyItemRangeInserted(
							start,
							newItems.first.size
						)

						loading = false
					}
				} catch (e: IOException) {
					runOnUiThread {
						loading = false
						setLoading(false)
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
					runOnUiThread {
						loading = false
						setLoading(false)
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

	private fun getData(initial: Boolean): Pair<Pair<List<RendererContainer>, UserData?>, String?> {
		when (type) {
			"channel" -> {
				val channel =
					if (initial && initialData != null) Gson().fromJson(
						initialData,
						object : TypeToken<ApiResponse<LightTubeChannel>>() {})!!
					else if (initial) api.getChannel(args, params ?: "home")
					else api.continueChannel(contKey ?: "")
				if (initial) userData = channel.userData
				else userData!!.channels.putAll(channel.userData?.channels ?: emptyMap())
				val contents = ArrayList(channel.data!!.contents)
				if (initial && params?.lowercase() == "home")
					contents.add(0, channel.data.getAsRenderer())
				val continuationRenderer = channel.data.contents.lastOrNull { it.type == "continuation" }
				val continuationRendererData =
					if (continuationRenderer != null) continuationRenderer.data as ContinuationRendererData else null
				return Pair(Pair(contents, channel.userData), continuationRendererData?.continuationToken)
			}

			else -> {
				return Pair(Pair(emptyList(), null), null)
			}
		}
	}

	override fun onScreenSizeChanged(newSize: Int) {
		Utils.rebindAllRecyclerViews(binding.recycler)
	}

	override fun onConfigurationChanged(newConfig: Configuration) {
		super.onConfigurationChanged(newConfig)

		(binding.recycler.adapter as RendererRecyclerAdapter)
			.notifyScreenRotated(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
	}
}