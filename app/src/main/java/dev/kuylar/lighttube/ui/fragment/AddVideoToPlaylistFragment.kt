package dev.kuylar.lighttube.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.JsonObject
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.Utils
import dev.kuylar.lighttube.api.LightTubeApi
import dev.kuylar.lighttube.api.models.PlaylistVisibility
import dev.kuylar.lighttube.databinding.FragmentAddVideoToPlaylistBinding
import dev.kuylar.lighttube.ui.activity.MainActivity
import dev.kuylar.lighttube.ui.adapter.PlaylistMenuAdapter
import kotlin.concurrent.thread

class AddVideoToPlaylistFragment(private val videoId: String) : BottomSheetDialogFragment() {
	private lateinit var binding: FragmentAddVideoToPlaylistBinding
	private lateinit var api: LightTubeApi
	private val items = ArrayList<JsonObject>()

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentAddVideoToPlaylistBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		api = (activity as MainActivity).getApi()

		binding.buttonNewPlaylist.setOnClickListener {
			Utils.showPlaylistDialog(
				requireContext(),
				layoutInflater,
				getString(R.string.create_playlist),
				getString(R.string.create_playlist_default_title),
				"",
				PlaylistVisibility.Private,
				getString(R.string.action_playlist_create),
				getString(R.string.action_cancel)
			) { _, title, description, visibility ->
				thread {
					api.createPlaylist(title, description, visibility)
					refreshData()
				}
			}
		}

		binding.recyclerPlaylists.layoutManager =
			LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
		binding.recyclerPlaylists.adapter =
			PlaylistMenuAdapter(items, layoutInflater) { id, title ->
				thread {
					try {
						api.addVideoToPlaylist(id, videoId)
						activity?.runOnUiThread {
							Toast.makeText(
								requireContext(),
								getString(R.string.playlist_video_added, title),
								Toast.LENGTH_LONG
							).show()
							dismiss()
						}
					} catch (e: Exception) {
						activity?.runOnUiThread {
							Toast.makeText(
								requireContext(),
								getString(R.string.playlist_video_add_failed, e.message),
								Toast.LENGTH_LONG
							).show()
							dismiss()
						}
					}
				}
			}

		thread {
			refreshData()
		}
	}

	@SuppressLint("NotifyDataSetChanged")
	private fun refreshData() {
		val playlists = api.getLibraryPlaylists()
		items.clear()
		items.addAll(playlists.data!!)
		activity?.runOnUiThread {
			binding.recyclerPlaylists.adapter!!.notifyDataSetChanged()
		}
	}
}