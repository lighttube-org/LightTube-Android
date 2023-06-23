package dev.kuylar.lighttube.ui.viewholder

import android.content.Intent
import android.os.Bundle
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.google.gson.JsonObject
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.Utils
import dev.kuylar.lighttube.api.models.LightTubeVideo
import dev.kuylar.lighttube.databinding.RendererSlimVideoInfoBinding
import dev.kuylar.lighttube.ui.activity.MainActivity
import kotlin.concurrent.thread

open class SlimVideoInfoRenderer(private val binding: RendererSlimVideoInfoBinding) :
	RendererViewHolder(binding.root) {
	override fun bind(item: JsonObject) {
		val activity = binding.root.context as MainActivity
		val video = Gson().fromJson(item, LightTubeVideo::class.java)
		binding.videoTitle.text = video.title
		binding.channelTitle.text = video.channel.title
		binding.videoViews.text = video.viewCount
		binding.videoUploaded.text = video.dateText
		binding.buttonLike.text = video.likeCount
		Glide
			.with(activity)
			.load(video.channel.avatar)
			.into(binding.channelAvatar)

		binding.buttonComments.setOnClickListener {
			activity.player.setSheets(details = false, comments = true)
		}
		binding.videoDetails.setOnClickListener {
			activity.player.setSheets(details = true, comments = false)
		}

		binding.channelAvatar.setOnClickListener {
			activity.miniplayer.state = BottomSheetBehavior.STATE_COLLAPSED
			val b = Bundle()
			b.putString("id", video.channel.id)
			activity.findNavController(R.id.nav_host_fragment_activity_main)
				.navigate(R.id.navigation_channel, b)
		}

		binding.buttonShare.setOnClickListener {
			val sendIntent: Intent = Intent().apply {
				action = Intent.ACTION_SEND
				putExtra(Intent.EXTRA_TEXT, "https://youtu.be/${video.id}")
				type = "text/plain"
			}

			val shareIntent = Intent.createChooser(sendIntent, null)
			activity.startActivity(shareIntent)
		}

		thread {
			val dislikes = Utils.getDislikeCount(video.id)
			if (dislikes != -1L)
				activity.runOnUiThread {
					binding.buttonDislike.text = dislikes.toString()
				}
		}
	}
}