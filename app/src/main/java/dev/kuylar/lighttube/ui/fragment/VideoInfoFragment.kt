package dev.kuylar.lighttube.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.api.LightTubeApi
import dev.kuylar.lighttube.api.models.LightTubeVideo
import dev.kuylar.lighttube.databinding.FragmentVideoInfoBinding
import dev.kuylar.lighttube.ui.activity.MainActivity
import kotlin.concurrent.thread

class VideoInfoFragment : Fragment() {
	private lateinit var detailsSheet: BottomSheetBehavior<LinearLayout>
	private lateinit var id: String
	private var playlistId: String? = null
	private lateinit var binding: FragmentVideoInfoBinding
	private lateinit var api: LightTubeApi

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		api = (requireActivity() as MainActivity).api
		arguments?.let {
			id = it.getString("id", "")
			playlistId = it.getString("playlistId", "")
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentVideoInfoBinding.inflate(inflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		if (id.isNotBlank())
			thread {
				val video = api.getVideo(id, playlistId)
				activity?.runOnUiThread {
					fillData(video.data!!)
				}
			}

		detailsSheet = BottomSheetBehavior.from(binding.sheetVideoDetails)

		//todo: placeholder shimmer thing?
	}

	private fun fillData(video: LightTubeVideo) {
		binding.videoTitle.text = video.title
		binding.channelTitle.text = video.channel.title
		binding.videoViews.text = video.viewCount
		binding.videoUploaded.text = video.dateText
		binding.buttonLike.text = video.likeCount
		Glide
			.with(this)
			.load(video.channel.avatar)
			.into(binding.channelAvatar)

		requireActivity().supportFragmentManager.beginTransaction().apply {
			val f = VideoDetailsFragment(video)
			replace(R.id.video_info_fragment, f)
		}.commit()

		binding.videoDetails.setOnClickListener {
			detailsSheet.state = BottomSheetBehavior.STATE_EXPANDED
		}
	}
}