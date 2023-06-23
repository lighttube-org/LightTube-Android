package dev.kuylar.lighttube.ui.viewholder

import android.view.View
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.Utils
import dev.kuylar.lighttube.databinding.RendererVideoBinding
import dev.kuylar.lighttube.ui.activity.MainActivity

class VideoRenderer(val binding: RendererVideoBinding) : RendererViewHolder(binding.root) {
	override fun bind(item: JsonObject) {
		binding.videoTitle.text = item.getAsJsonPrimitive("title").asString
		var durText = item.getAsJsonPrimitive("duration").asString
		if (durText.startsWith("00:")) durText = durText.substring(3)
		if (durText == "00:00") {
			binding.videoDuration.background = AppCompatResources.getDrawable(
				binding.root.context,
				R.drawable.live_video_duration_background
			)
			binding.videoDuration.text = binding.root.context.getString(R.string.live).uppercase()
		} else {
			binding.videoDuration.text = durText
		}

		Glide
			.with(binding.root)
			.load(Utils.getBestImageUrlJson(item.getAsJsonArray("thumbnails")))
			.into(binding.videoThumbnail)

		if (item.has("channel"))
			if (!item.getAsJsonObject("channel").get("avatar").isJsonNull)
				Glide
					.with(binding.root)
					.load(item.getAsJsonObject("channel").asJsonObject.getAsJsonPrimitive("avatar").asString)
					.into(binding.channelAvatar)
			else
				binding.channelAvatar.visibility = View.GONE

		val items = ArrayList<String>()
		if (item.has("channel") && !item.getAsJsonObject("channel").get("title").isJsonNull)
			items.add(item.getAsJsonObject("channel").asJsonObject.getAsJsonPrimitive("title").asString)

		items.add(item.getAsJsonPrimitive("viewCount").asString)

		if (!item.get("published").isJsonNull)
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