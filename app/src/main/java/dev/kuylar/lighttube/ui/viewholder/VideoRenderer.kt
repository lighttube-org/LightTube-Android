package dev.kuylar.lighttube.ui.viewholder

import android.view.View
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import com.bumptech.glide.Glide
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.Utils
import dev.kuylar.lighttube.api.models.UserData
import dev.kuylar.lighttube.api.models.renderers.RendererContainer
import dev.kuylar.lighttube.api.models.renderers.VideoRendererData
import dev.kuylar.lighttube.databinding.RendererVideoBinding
import dev.kuylar.lighttube.ui.activity.MainActivity

class VideoRenderer(val binding: RendererVideoBinding) : RendererViewHolder(binding.root) {
	override fun bind(renderer: RendererContainer, userData: UserData?) {
		val item = renderer.data as VideoRendererData
		binding.videoTitle.text = item.title
		var durText = item.duration
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
			.load(Utils.getBestImageUrl(item.thumbnails).takeIf { it.isNotEmpty() }
				?: "https://i.ytimg.com/vi/${item.videoId}/hqdefault.jpg")
			.into(binding.videoThumbnail)

		if (item.author?.avatar != null)
			Glide
				.with(binding.root)
				.load(Utils.getBestImageUrl(item.author.avatar))
				.into(binding.channelAvatar)
		else
			binding.channelAvatar.visibility = View.GONE

		val items = ArrayList<String>()
		if (item.author?.title != null)
			items.add(item.author.title)

		if (item.viewCountText != null)
			items.add(item.viewCountText)

		if (item.publishedText != null)
			items.add(item.publishedText)

		binding.videoSubtitle.text = items.joinToString(" • ")

		binding.root.setOnClickListener {
			// bad idea? idk
			if (binding.root.context is MainActivity)
				(binding.root.context as MainActivity).getPlayer().playVideo(item.videoId)
			else
				Toast.makeText(binding.root.context, "uhh click :3", Toast.LENGTH_LONG).show()
		}
	}
}