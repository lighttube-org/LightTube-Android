package dev.kuylar.lighttube.ui.viewholder

import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.databinding.RendererVideoBinding

class VideoRenderer(val binding: RendererVideoBinding) : RendererViewHolder(binding.root) {
	override fun bind(item: JsonObject) {
		binding.videoTitle.text = item.getAsJsonPrimitive("title").asString
		binding.videoSubtitle.text = binding.root.context.getString(
			R.string.template_video_subtitle,
			item.getAsJsonObject("channel").asJsonObject.getAsJsonPrimitive("title").asString,
			item.getAsJsonPrimitive("viewCount").asString,
			item.getAsJsonPrimitive("published").asString
		)
		var durText = item.getAsJsonPrimitive("duration").asString
		if (durText.startsWith("00:")) durText = durText.substring(3)
		binding.videoDuration.text = durText

		Glide
			.with(binding.root)
			.load(item.getAsJsonArray("thumbnails")[0].asJsonObject.getAsJsonPrimitive("url").asString)
			.into(binding.videoThumbnail)

		Glide
			.with(binding.root)
			.load(item.getAsJsonObject("channel").asJsonObject.getAsJsonPrimitive("avatar").asString)
			.into(binding.channelAvatar)
	}
}