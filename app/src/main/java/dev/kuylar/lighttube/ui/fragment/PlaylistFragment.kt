package dev.kuylar.lighttube.ui.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.gson.JsonObject
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.Utils
import dev.kuylar.lighttube.api.LightTubeApi
import dev.kuylar.lighttube.api.models.LightTubeException
import dev.kuylar.lighttube.api.models.LightTubePlaylist
import dev.kuylar.lighttube.api.models.PlaylistVisibility
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
		view.viewTreeObserver.addOnGlobalLayoutListener(object :
			ViewTreeObserver.OnGlobalLayoutListener {
			override fun onGlobalLayout() {
				view.viewTreeObserver.removeOnGlobalLayoutListener(this)
				binding.playlistInfoSidebar.layoutParams = LinearLayout.LayoutParams(
					Utils.getSidebarWidth(activity as MainActivity, binding.root.width),
					LinearLayout.LayoutParams.MATCH_PARENT
				)
			}
		})
		if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			adapter.notifyScreenRotated(true)
			binding.playlistInfoSidebar.visibility = View.VISIBLE
		} else {
			binding.playlistInfoSidebar.visibility = View.GONE
		}
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
					if (initial) {
						items.add(0, playlist.data!!.getAsRenderer(api))
						runOnUiThread {
							fillSidebar(playlist.data)
						}
					}
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

	private fun fillSidebar(playlist: LightTubePlaylist) {
		val activity = binding.root.context as MainActivity
		binding.playlistInfo.playlistTitle.text = playlist.title
		binding.playlistInfo.playlistAuthor.text = playlist.channel.title
		binding.playlistInfo.playlistVideoCount.text = playlist.videoCountText
		if (playlist.description.isNullOrEmpty())
			binding.playlistInfo.playlistDescription.visibility = View.GONE
		else
			binding.playlistInfo.playlistDescription.text = playlist.description

		Glide
			.with(activity)
			.load(Utils.getBestImageUrl(playlist.thumbnails))
			.into(binding.playlistInfo.playlistThumbnail)

		binding.playlistInfo.buttonPlayAll.setOnClickListener {
			activity.getPlayer().playVideo(playlist.videos.first().asJsonObject.getAsJsonPrimitive("id").asString)
		}

		binding.playlistInfo.buttonShuffle.setOnClickListener {
			activity.getPlayer().playVideo(playlist.videos.first().asJsonObject.getAsJsonPrimitive("id").asString)
		}

		if (playlist.editable) {
			(binding.playlistInfo.root.context as MainActivity).apply {
				binding.playlistInfo.buttonEditPlaylist.setOnClickListener {
					Utils.showPlaylistDialog(
						this,
						layoutInflater,
						getString(R.string.edit_playlist_title),
						playlist.title,
						playlist.description ?: "",
						PlaylistVisibility.Private,
						getString(R.string.edit_playlist_submit),
						getString(R.string.edit_playlist_cancel),
					) { dialog, title, description, visibility ->
						thread {
							getApi().updatePlaylist(
								playlist.id,
								title,
								description,
								visibility
							)
							runOnUiThread {
								dialog.dismiss()
							}
						}
					}
				}

				binding.playlistInfo.buttonDeletePlaylist.setOnClickListener {
					MaterialAlertDialogBuilder(binding.playlistInfo.root.context)
						.setTitle(getString(R.string.delete_playlist_title, playlist.title))
						.setMessage(R.string.delete_playlist_body)
						.setPositiveButton(R.string.delete_playlist) { dialog, _ ->
							thread {
								getApi().deletePlaylist(playlist.id)
								runOnUiThread {
									Toast.makeText(
										this,
										R.string.delete_playlist_success,
										Toast.LENGTH_LONG
									).show()
									findNavController(R.id.nav_host_fragment_activity_main).navigateUp()
									dialog.dismiss()
								}
							}
						}
						.setNegativeButton(R.string.delete_playlist_cancel) { dialog, _ ->
							dialog.dismiss()
						}
						.show()
				}
			}
		} else {
			binding.playlistInfo.buttonEditPlaylist.visibility = View.GONE
			binding.playlistInfo.buttonDeletePlaylist.visibility = View.GONE
		}
	}

	override fun onConfigurationChanged(newConfig: Configuration) {
		super.onConfigurationChanged(newConfig)

		if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			(binding.recyclerPlaylist.adapter as RendererRecyclerAdapter)
				.notifyScreenRotated(true)
			binding.playlistInfoSidebar.visibility = View.VISIBLE
		} else {
			(binding.recyclerPlaylist.adapter as RendererRecyclerAdapter)
				.notifyScreenRotated(false)
			binding.playlistInfoSidebar.visibility = View.GONE
		}
	}
}