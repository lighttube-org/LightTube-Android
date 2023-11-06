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
import dev.kuylar.lighttube.databinding.FragmentPlaylistBinding
import dev.kuylar.lighttube.ui.activity.MainActivity
import dev.kuylar.lighttube.ui.adapter.RendererRecyclerAdapter
import java.io.IOException
import kotlin.concurrent.thread

class PlaylistFragment : Fragment() {
	private lateinit var id: String
	private lateinit var binding: FragmentPlaylistBinding
	private val items: MutableList<JsonObject> = mutableListOf()
	private lateinit var api: LightTubeApi
	private var loading = false
	private var contKey: String? = null

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentPlaylistBinding.inflate(layoutInflater)
		arguments?.let {
			id = it.getString("id")!!
		}
		(activity as MainActivity).apply {
			this@PlaylistFragment.api = getApi()
			supportActionBar?.title = ""
		}
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		val adapter = RendererRecyclerAdapter(items)
		binding.recyclerPlaylist.layoutManager = LinearLayoutManager(context)
		binding.recyclerPlaylist.adapter = adapter
		binding.recyclerPlaylist.itemAnimator = null
		binding.recyclerPlaylist.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
		(activity as MainActivity).apply {
			if (loading) return
			if (!initial && contKey == null) return
			loading = true
			setLoading(true)
			thread {
				try {
					val playlist =
						if (initial) api.getPlaylist(id) else api.continuePlaylist(contKey!!)
					val start = items.size
					(binding.recyclerPlaylist.adapter!! as RendererRecyclerAdapter).updateUserData(playlist.userData)
					if (initial)
						items.add(0, playlist.data!!.getAsRenderer(api))
					playlist.data!!.alerts.forEach {
						items.add(JsonObject().apply {
							addProperty("type", "playlistAlertRenderer")
							addProperty("text", it)
						})
					}
					items.addAll(playlist.data.videos.map {
						it.addProperty("playlistId", this@PlaylistFragment.id)
						it
					})
					contKey = playlist.data.continuation
					if (activity == null) return@thread
					runOnUiThread {
						setLoading(false)
						binding.recyclerPlaylist.adapter!!.notifyItemRangeInserted(
							start,
							playlist.data.videos.size + 1
						)
						loading = false
					}
				} catch (e: IOException) {
					if (activity == null) return@thread
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
					if (activity == null) return@thread
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
}