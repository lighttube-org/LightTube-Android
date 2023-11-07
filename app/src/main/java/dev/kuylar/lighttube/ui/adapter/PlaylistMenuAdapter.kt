package dev.kuylar.lighttube.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import dev.kuylar.lighttube.databinding.ItemMenuPlaylistBinding

class PlaylistMenuAdapter(
	private val items: List<JsonObject>,
	private val layoutInflater: LayoutInflater,
	private val onClick: (id: String, title: String) -> Unit
) : RecyclerView.Adapter<PlaylistMenuAdapter.ViewHolder>() {
	class ViewHolder(private val binding: ItemMenuPlaylistBinding) :
		RecyclerView.ViewHolder(binding.root) {
		fun bind(playlist: JsonObject, onClick: (id: String, title: String) -> Unit) {
			binding.root.setOnClickListener {
				onClick(
					playlist.getAsJsonPrimitive("id").asString,
					playlist.getAsJsonPrimitive("title").asString
				)
			}

			binding.playlistTitle.text = playlist.getAsJsonPrimitive("title").asString
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
		ViewHolder(ItemMenuPlaylistBinding.inflate(layoutInflater, parent, false))

	override fun getItemCount() = items.size

	override fun onBindViewHolder(holder: ViewHolder, position: Int) =
		holder.bind(items[position], onClick)
}