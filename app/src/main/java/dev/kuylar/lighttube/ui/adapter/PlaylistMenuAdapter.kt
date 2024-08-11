package dev.kuylar.lighttube.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.kuylar.lighttube.api.models.renderers.PlaylistRendererData
import dev.kuylar.lighttube.api.models.renderers.RendererContainer
import dev.kuylar.lighttube.databinding.ItemMenuPlaylistBinding

class PlaylistMenuAdapter(
	private val items: List<RendererContainer>,
	private val layoutInflater: LayoutInflater,
	private val onClick: (id: String, title: String) -> Unit
) : RecyclerView.Adapter<PlaylistMenuAdapter.ViewHolder>() {
	class ViewHolder(private val binding: ItemMenuPlaylistBinding) :
		RecyclerView.ViewHolder(binding.root) {
		fun bind(renderer: RendererContainer, onClick: (id: String, title: String) -> Unit) {
			val playlist = (renderer.data as PlaylistRendererData)
			binding.root.setOnClickListener {
				onClick(
					playlist.playlistId,
					playlist.title
				)
			}

			binding.playlistTitle.text = playlist.title
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
		ViewHolder(ItemMenuPlaylistBinding.inflate(layoutInflater, parent, false))

	override fun getItemCount() = items.size

	override fun onBindViewHolder(holder: ViewHolder, position: Int) =
		holder.bind(items[position], onClick)
}