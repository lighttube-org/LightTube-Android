package dev.kuylar.lighttube.ui.viewholder

import android.content.Intent
import android.text.Html
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.google.gson.JsonObject
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.Utils
import dev.kuylar.lighttube.api.models.LightTubeVideo
import dev.kuylar.lighttube.api.models.SubscriptionInfo
import dev.kuylar.lighttube.api.models.UserData
import dev.kuylar.lighttube.databinding.RendererSlimVideoInfoBinding
import dev.kuylar.lighttube.ui.activity.MainActivity
import dev.kuylar.lighttube.ui.fragment.AddVideoToPlaylistFragment
import kotlin.concurrent.thread

open class SlimVideoInfoRenderer(private val binding: RendererSlimVideoInfoBinding) :
	RendererViewHolder(binding.root) {
	override fun bind(item: JsonObject, userData: UserData?) {
		val activity = binding.root.context as MainActivity
		val video = Gson().fromJson(item, LightTubeVideo::class.java)
		var subscriptionInfo =
			userData?.channels?.get(video.channel.id) ?: SubscriptionInfo(
				subscribed = false,
				notifications = false
			)
		binding.videoTitle.text = video.title
		binding.channelTitle.text = video.channel.title
		binding.channelSubscribers.text = video.channel.subscribers
		binding.videoViews.text = video.viewCount
		binding.videoUploaded.text = video.dateText
		binding.buttonLike.text = video.likeCount
		if (video.showCommentsButton) {
			if (video.firstComment != null) {
				binding.commentsLoading.visibility = View.GONE
				binding.commentsFirst.visibility = View.VISIBLE

				Glide.with(binding.root)
					.load(video.firstComment!!.first)
					.into(binding.commentAvatar)
				binding.commentText.text =
					Html.fromHtml(video.firstComment!!.second, Html.FROM_HTML_MODE_LEGACY)
				binding.commentsCountBullet.visibility = View.VISIBLE
				binding.commentsCount.text = video.commentCount?.takeIf { it.isNotEmpty() }
					?: "${video.firstComment!!.third}+"
				binding.cardComments.setOnClickListener {
					activity.player.setSheets(details = false, comments = true)
				}
			} else {
				binding.spinnerComments.visibility = View.GONE
				binding.textCommentsLoading.setText(R.string.comments_loading_fail)
			}
		}

		Glide
			.with(activity)
			.load(video.channel.avatar)
			.into(binding.channelAvatar)

		binding.videoDetails.setOnClickListener {
			activity.getPlayer().setSheets(details = true, comments = false)
		}

		binding.channelContainer.setOnClickListener {
			activity.miniplayer.state = BottomSheetBehavior.STATE_COLLAPSED
			activity.findNavController(R.id.nav_host_fragment_activity_main)
				.navigate(R.id.navigation_channel, bundleOf(Pair("id", video.channel.id)))
		}

		binding.buttonSubscribe.setOnClickListener {
			Utils.subscribe(
				binding.root.context,
				video.channel.id,
				subscriptionInfo,
				binding.buttonSubscribe
			) {
				subscriptionInfo = it
			}
		}

		Utils.updateSubscriptionButton(
			binding.root.context,
			binding.buttonSubscribe,
			subscriptionInfo
		)

		binding.buttonShare.setOnClickListener {
			val sendIntent: Intent = Intent().apply {
				action = Intent.ACTION_SEND
				putExtra(Intent.EXTRA_TEXT, "https://youtu.be/${video.id}")
				type = "text/plain"
			}

			val shareIntent = Intent.createChooser(sendIntent, null)
			activity.startActivity(shareIntent)
		}

		binding.buttonSave.setOnClickListener {
			val sheet = AddVideoToPlaylistFragment(video.id)
			sheet.show(activity.supportFragmentManager, null)
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