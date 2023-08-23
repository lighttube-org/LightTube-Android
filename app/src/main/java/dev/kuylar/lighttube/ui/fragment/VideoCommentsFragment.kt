package dev.kuylar.lighttube.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.JsonObject
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.api.LightTubeApi
import dev.kuylar.lighttube.api.models.LightTubeException
import dev.kuylar.lighttube.databinding.FragmentVideoCommentsBinding
import dev.kuylar.lighttube.ui.activity.MainActivity
import dev.kuylar.lighttube.ui.adapter.RendererRecyclerAdapter
import java.io.IOException
import kotlin.concurrent.thread

class VideoCommentsFragment : Fragment() {
	private var commentsContinuation: String? = null
	private var loading: Boolean = false
	private val items: MutableList<JsonObject> = mutableListOf()
	private lateinit var api: LightTubeApi
	private lateinit var binding: FragmentVideoCommentsBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		arguments?.let {
			commentsContinuation = it.getString("commentsContinuation")
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		api = (activity as MainActivity).api
		binding = FragmentVideoCommentsBinding.inflate(inflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		val adapter = RendererRecyclerAdapter(items)
		binding.recyclerComments.layoutManager = LinearLayoutManager(context)
		binding.recyclerComments.adapter = adapter
		binding.recyclerComments.itemAnimator = null
		binding.recyclerComments.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
		if (commentsContinuation == null) return
		loading = true
		(activity as MainActivity).setLoading(true)
		thread {
			try {
				val comments = api.getComments(commentsContinuation!!)
				val start = items.size
				items.addAll(comments.data!!.contents)
				commentsContinuation = comments.data.continuation
				if (activity == null) return@thread
				activity?.runOnUiThread {
					(activity as MainActivity).setLoading(false)
					binding.recyclerComments.adapter!!.notifyItemRangeInserted(
						start,
						comments.data.contents.size
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
					sb.setAction(R.string.action_retry) {
						loadMore()
						sb.dismiss()
					}
					sb.show()
				}
			}
		}
	}
}