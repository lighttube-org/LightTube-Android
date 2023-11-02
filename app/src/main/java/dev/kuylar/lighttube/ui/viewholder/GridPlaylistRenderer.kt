package dev.kuylar.lighttube.ui.viewholder

import android.app.Activity
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.Utils
import dev.kuylar.lighttube.api.models.UserData
import dev.kuylar.lighttube.databinding.RendererGridPlaylistBinding
import java.text.DecimalFormat

class GridPlaylistRenderer(val binding: RendererGridPlaylistBinding) :
	RendererViewHolder(binding.root) {
	override fun bind(item: JsonObject, userData: UserData?) {
		binding.playlistTitle.text = item.getAsJsonPrimitive("title").asString
		binding.playlistVideoCount.text = binding.root.context.getString(
			R.string.template_videos,
			DecimalFormat().format(item.getAsJsonPrimitive("videoCount").asInt)
		)

		Glide
			.with(binding.root)
			.load(Utils.getBestImageUrlJson(item.getAsJsonArray("thumbnails")))
			.into(binding.playlistThumbnail)

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