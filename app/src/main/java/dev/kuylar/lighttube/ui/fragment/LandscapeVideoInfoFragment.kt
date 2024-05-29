package dev.kuylar.lighttube.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.Utils
import dev.kuylar.lighttube.api.LightTubeApi
import dev.kuylar.lighttube.api.models.LightTubeException
import dev.kuylar.lighttube.api.models.LightTubeVideo
import dev.kuylar.lighttube.api.models.SubscriptionInfo
import dev.kuylar.lighttube.api.models.UserData
import dev.kuylar.lighttube.databinding.FragmentLandscapeVideoInfoBinding
import dev.kuylar.lighttube.ui.VideoPlayerManager
import dev.kuylar.lighttube.ui.activity.MainActivity
import java.io.IOException
import kotlin.concurrent.thread

class LandscapeVideoInfoFragment : Fragment() {
	private lateinit var binding: FragmentLandscapeVideoInfoBinding
	private lateinit var api: LightTubeApi
	private lateinit var player: VideoPlayerManager
	private lateinit var id: String
	private var playlistId: String? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		api = (requireActivity() as MainActivity).getApi()
		player = (requireActivity() as MainActivity).getPlayer()
		arguments?.let {
			id = it.getString("id")!!
			playlistId = it.getString("playlistId")
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentLandscapeVideoInfoBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		if (id.isNotBlank())
			thread {
				try {
					val video = api.getVideo(id, playlistId)
					activity?.runOnUiThread {
						fillData(video.data!!, video.userData)
					}
				} catch (e: IOException) {
					activity?.runOnUiThread {
						Snackbar.make(
							binding.root,
							R.string.error_connection,
							Snackbar.LENGTH_INDEFINITE
						).show()
					}
				} catch (e: LightTubeException) {
					activity?.runOnUiThread {
						Snackbar.make(
							binding.root,
							getString(R.string.error_lighttube, e.message),
							Snackbar.LENGTH_INDEFINITE
						).setTextMaxLines(2)
							.show()
					}
				}
			}

	}

	fun fillData(video: LightTubeVideo, userData: UserData?) {
		var subscriptionInfo =
			userData?.channels?.get(video.channel.id) ?: SubscriptionInfo(
				subscribed = false,
				notifications = false
			)
		val activity = activity as MainActivity
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
					activity.getPlayer().setSheets(details = false, comments = true)
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