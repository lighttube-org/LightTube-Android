package dev.kuylar.lighttube.ui.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.setPadding
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.Utils
import dev.kuylar.lighttube.api.models.PlaylistVisibility
import dev.kuylar.lighttube.api.models.UserData
import dev.kuylar.lighttube.databinding.ItemDividerBinding
import dev.kuylar.lighttube.databinding.ItemPlaylistBinding
import dev.kuylar.lighttube.ui.activity.MainActivity
import dev.kuylar.lighttube.ui.transformations.ThumbnailTransformation
import java.text.DecimalFormat
import kotlin.concurrent.thread

class LibraryAdapter(activity: Activity, private val items: List<JsonObject>) :
	RecyclerView.Adapter<RecyclerView.ViewHolder>() {
	private val layoutInflater = activity.layoutInflater

	private var userData = UserData(null, hashMapOf(), false, null)

	class ActionViewHolder(
		private val binding: ItemPlaylistBinding,
		private val action: String,
		private val layoutInflater: LayoutInflater
	) :
		RecyclerView.ViewHolder(binding.root) {
		fun bind() {
			binding.title.setText(actions[action]!!.first)

			if (action == "newPlaylist") {
				binding.subtitle.visibility = View.GONE
				binding.root.setOnClickListener {
					Utils.showPlaylistDialog(
						binding.root.context,
						layoutInflater,
						binding.root.resources.getString(R.string.create_playlist),
						binding.root.resources.getString(R.string.create_playlist_default_title),
						"",
						PlaylistVisibility.Private,
						binding.root.resources.getString(R.string.action_playlist_create),
						binding.root.resources.getString(R.string.action_cancel)
					) { _, title, description, visibility ->
						thread {
							with(binding.root.context as MainActivity) {
								val createPlaylist =
									getApi().createPlaylist(title, description, visibility)
								runOnUiThread {
									findNavController(R.id.nav_host_fragment_activity_main)
										.navigate(
											R.id.navigation_playlist, bundleOf(
												Pair("id", createPlaylist.data!!.id)
											)
										)
								}
							}
						}
					}
				}
			}

			binding.icon.setPadding(8 * (binding.root.resources.displayMetrics.density).toInt())
			binding.icon.setImageDrawable(
				ResourcesCompat.getDrawable(
					binding.root.resources,
					actions[action]!!.second,
					null
				)
			)
		}
	}

	class DividerViewHolder(binding: ItemDividerBinding) : RecyclerView.ViewHolder(binding.root)

	class PlaylistViewHolder(private val binding: ItemPlaylistBinding) :
		RecyclerView.ViewHolder(binding.root) {
		fun bind(item: JsonObject) {
			binding.title.text = item.getAsJsonPrimitive("title").asString
			binding.subtitle.text = binding.root.context.getString(
				R.string.template_videos,
				DecimalFormat().format(item.getAsJsonPrimitive("videoCount").asInt)
			)

			Glide
				.with(binding.root)
				.load(Utils.getBestImageUrlJson(item.getAsJsonArray("thumbnails")))
				.transform(ThumbnailTransformation())
				.into(binding.icon)

			binding.root.setOnClickListener {
				(binding.root.context as Activity).findNavController(R.id.nav_host_fragment_activity_main)
					.navigate(
						R.id.navigation_playlist, bundleOf(
							Pair("id", item.getAsJsonPrimitive("id").asString)
						)
					)
			}
		}
	}

	override fun getItemViewType(position: Int): Int {
		return if (position < 2) position
		else if (position == 2) 100
		else 101
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when {
		(viewType < 100) -> ActionViewHolder(
			ItemPlaylistBinding.inflate(
				layoutInflater,
				parent,
				false
			),
			actions.keys.toList()[viewType],
			layoutInflater
		)

		(viewType == 100) -> DividerViewHolder(
			ItemDividerBinding.inflate(
				layoutInflater,
				parent,
				false
			)
		)

		else -> PlaylistViewHolder(ItemPlaylistBinding.inflate(layoutInflater, parent, false))
	}

	override fun getItemCount() = items.size + 1 /* Divider */ + actions.size

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		if (holder is ActionViewHolder)
			holder.bind()
		else if (holder is PlaylistViewHolder)
			holder.bind(items[position - actions.size - 1].asJsonObject)
	}

	fun updateUserData(newUserData: UserData?) {
		if (newUserData == null) return
		userData.user = newUserData.user
		userData.channels.putAll(newUserData.channels)
		userData.editable = newUserData.editable
		userData.playlistId = newUserData.playlistId
	}

	companion object {
		val actions = mapOf(
			Pair("downloads", Pair(R.string.library_downloads, R.drawable.ic_download)),
			Pair("newPlaylist", Pair(R.string.create_playlist, R.drawable.ic_create))
		)
	}
}