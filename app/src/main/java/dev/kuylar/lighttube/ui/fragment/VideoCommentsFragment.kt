package dev.kuylar.lighttube.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.Utils
import dev.kuylar.lighttube.api.LightTubeApi
import dev.kuylar.lighttube.api.models.LightTubeException
import dev.kuylar.lighttube.api.models.SortOrder
import dev.kuylar.lighttube.api.models.renderers.CommentRendererData
import dev.kuylar.lighttube.api.models.renderers.RendererContainer
import dev.kuylar.lighttube.databinding.FragmentVideoCommentsBinding
import dev.kuylar.lighttube.ui.VideoPlayerManager
import dev.kuylar.lighttube.ui.activity.MainActivity
import dev.kuylar.lighttube.ui.adapter.RendererRecyclerAdapter
import java.io.IOException
import kotlin.concurrent.thread

class VideoCommentsFragment : Fragment() {
	private var commentsContinuation: String? = null
	private var loading: Boolean = false
	private val items: MutableList<RendererContainer> = mutableListOf()
	private lateinit var api: LightTubeApi
	private lateinit var player: VideoPlayerManager
	private lateinit var binding: FragmentVideoCommentsBinding
	private lateinit var id: String

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		arguments?.let {
			id = it.getString("id")!!
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentVideoCommentsBinding.inflate(inflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		api = (activity as MainActivity).getApi()
		player = (activity as MainActivity).getPlayer()
		val adapter = RendererRecyclerAdapter(items)
		binding.recyclerComments.layoutManager = LinearLayoutManager(context)
		binding.recyclerComments.adapter = adapter
		binding.recyclerComments.itemAnimator = null
		binding.recyclerComments.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
		if (!initial && commentsContinuation == null) return
		loading = true
		(activity as MainActivity).setLoading(true)
		thread {
			try {
				val comments = if (initial) api.getComments(
					id,
					SortOrder.TopComments
				) else api.continueComments(commentsContinuation!!)
				if (initial) with(comments.data!!.results[0]) {
					val comment = this.data as CommentRendererData
					try {
						player.showCommentsButton(
							Utils.getBestImageUrl(comment.owner.avatar),
							comment.content,
							comments.data.results.size // todo: get this from *somewhere*
						)
					} catch (_: Exception) {
						player.showCommentsButton()
					}
					loading = false
				}
				val start = items.size
				items.addAll(comments.data!!.results)
				commentsContinuation = comments.data.continuationToken
				if (activity == null) return@thread
				activity?.runOnUiThread {
					(activity as MainActivity).setLoading(false)
					binding.recyclerComments.adapter!!.notifyItemRangeInserted(
						start,
						comments.data.results.size
					)
					loading = false
				}
			} catch (e: IOException) {
				if (activity == null) return@thread
				activity?.runOnUiThread {
					player.showCommentsButton()
					loading = false
					(activity as MainActivity).setLoading(false)
					val sb = Snackbar.make(
						binding.root,
						R.string.error_connection,
						Snackbar.LENGTH_INDEFINITE
					)
					sb.setAction(R.string.action_retry) {
						loadMore(initial)
						sb.dismiss()
					}
					sb.show()
				}
			} catch (e: LightTubeException) {
				if (activity == null) return@thread
				activity?.runOnUiThread {
					player.showCommentsButton()
					loading = false
					(activity as MainActivity).setLoading(false)
				}
			}
		}
	}
}