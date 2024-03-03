package dev.kuylar.lighttube.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.gson.JsonArray
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.api.LightTubeApi
import dev.kuylar.lighttube.api.models.LightTubeException
import dev.kuylar.lighttube.databinding.FragmentLibraryBinding
import dev.kuylar.lighttube.ui.VideoPlayerManager
import dev.kuylar.lighttube.ui.activity.MainActivity
import dev.kuylar.lighttube.ui.adapter.RendererRecyclerAdapter
import java.io.IOException
import kotlin.concurrent.thread

class LibraryFragment : Fragment() {
	private val items: MutableList<JsonObject> = mutableListOf()
	private lateinit var api: LightTubeApi
	private lateinit var player: VideoPlayerManager
	private lateinit var binding: FragmentLibraryBinding

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentLibraryBinding.inflate(inflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		(activity as MainActivity).apply {
			this@LibraryFragment.api = getApi()
			this@LibraryFragment.player = getPlayer()
		}
		val adapter = RendererRecyclerAdapter(items)
		binding.recyclerLibrary.layoutManager = LinearLayoutManager(context)
		binding.recyclerLibrary.adapter = adapter
		binding.recyclerLibrary.itemAnimator = null
		if (items.size == 0)
			loadData()
	}

	private fun loadData() {
		(activity as MainActivity).apply {
			setLoading(true)
			thread {
				try {
					val playlists = api.getLibraryPlaylists()
					items.add(0, JsonObject().apply {
						addProperty("type", "gridPlaylistRenderer")
						addProperty("id", "!ACTION_NewPlaylist")
						addProperty("title", getString(R.string.create_playlist))
						addProperty("videoCount", 0)
						add("thumbnails", JsonArray().apply {
							add(JsonObject().apply {
								addProperty("width", 0)
								addProperty("height", 0)
								addProperty("url", "")
							})
						})
						add("channel", JsonObject().apply {
							add("id", JsonNull.INSTANCE)
							add("title", JsonNull.INSTANCE)
							add("avatar", JsonNull.INSTANCE)
							add("subscribers", JsonNull.INSTANCE)
							add("badges", JsonArray(0))
						})
					})
					items.addAll(playlists.data ?: emptyList())
					(binding.recyclerLibrary.adapter as RendererRecyclerAdapter).updateUserData(
						playlists.userData
					)
					runOnUiThread {
						binding.recyclerLibrary.adapter!!.notifyItemRangeInserted(
							0,
							playlists.data?.size ?: 0
						)
						setLoading(false)
					}
				} catch (e: IOException) {
					runOnUiThread {
						setLoading(false)
						val sb = Snackbar.make(
							binding.root,
							R.string.error_connection,
							Snackbar.LENGTH_INDEFINITE
						)
						sb.setAnchorView(R.id.nav_view)
						sb.setAction(R.string.action_retry) {
							loadData()
							sb.dismiss()
						}
						sb.show()
					}
				} catch (e: LightTubeException) {
					runOnUiThread {
						setLoading(false)
						val sb = Snackbar.make(
							binding.root,
							getString(R.string.error_lighttube, e.message),
							Snackbar.LENGTH_INDEFINITE
						)
						sb.setTextMaxLines(2)
						sb.setAnchorView(R.id.nav_view)
						sb.setAction(R.string.action_retry) {
							loadData()
							sb.dismiss()
						}
						sb.show()
					}
				}
			}
		}
	}
}