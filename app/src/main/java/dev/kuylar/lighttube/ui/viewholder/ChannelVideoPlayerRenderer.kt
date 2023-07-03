package dev.kuylar.lighttube.ui.viewholder

import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import dev.kuylar.lighttube.databinding.RendererVideoBinding
import dev.kuylar.lighttube.ui.activity.MainActivity

class ChannelVideoPlayerRenderer(val binding: RendererVideoBinding) :
	RendererViewHolder(binding.root) {
	override fun bind(item: JsonObject) {
		binding.videoTitle.text = item.getAsJsonPrimitive("title").asString
		binding.videoDuration.visibility = View.GONE
		binding.channelAvatar.visibility = View.GONE

		Glide
			.with(binding.root)
			.load("https://i.ytimg.com/vi/${item.getAsJsonPrimitive("id").asString}/maxresdefault.jpg")
			.into(binding.videoThumbnail)

		val items = ArrayList<String>()
		items.add(item.getAsJsonPrimitive("viewCount").asString)
		items.add(item.get("published").asString)
		binding.videoSubtitle.text = items.joinToString(" • ")

		binding.root.setOnClickListener {
			// bad idea? idk
			if (binding.root.context is MainActivity)
				(binding.root.context as MainActivity).player.playVideo(item.getAsJsonPrimitive("id").asString)
			else
				Toast.makeText(binding.root.context, "uhh click :3", Toast.LENGTH_LONG).show()
		}
	}
}