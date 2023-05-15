package dev.kuylar.lighttube.ui.viewholder

import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.databinding.RendererVideoBinding
import dev.kuylar.lighttube.ui.activity.MainActivity

class VideoRenderer(val binding: RendererVideoBinding) : RendererViewHolder(binding.root) {
	override fun bind(item: JsonObject) {
		binding.videoTitle.text = item.getAsJsonPrimitive("title").asString
		val p = item.getAsJsonPrimitive("published")
		var durText = item.getAsJsonPrimitive("duration").asString
		if (durText.startsWith("00:")) durText = durText.substring(3)
		binding.videoDuration.text = durText

		Glide
			.with(binding.root)
			.load(item.getAsJsonArray("thumbnails")[0].asJsonObject.getAsJsonPrimitive("url").asString)
			.into(binding.videoThumbnail)

		if (item.has("channel")) {
			if (!item.getAsJsonObject("channel").get("avatar").isJsonNull)
				Glide
					.with(binding.root)
					.load(item.getAsJsonObject("channel").asJsonObject.getAsJsonPrimitive("avatar").asString)
					.into(binding.channelAvatar)
			else
				binding.channelAvatar.visibility = View.GONE
			binding.videoSubtitle.text = binding.root.context.getString(
				R.string.template_video_subtitle,
				item.getAsJsonObject("channel").asJsonObject.getAsJsonPrimitive("title").asString,
				item.getAsJsonPrimitive("viewCount").asString,
				if (p.isJsonNull) "" else p.asString
			)
		} else {
			binding.videoSubtitle.text = binding.root.context.getString(
				R.string.template_video_subtitle_no_channel,
				item.getAsJsonPrimitive("viewCount").asString,
				if (p.isJsonNull) "" else p.asString
			)
		}

		binding.root.setOnClickListener {
			// bad idea? idk
			if (binding.root.context is MainActivity)
				(binding.root.context as MainActivity).player.playVideo(item.getAsJsonPrimitive("id").asString)
			else
				Toast.makeText(binding.root.context, "uhh click :3", Toast.LENGTH_LONG).show()
		}
	}
}