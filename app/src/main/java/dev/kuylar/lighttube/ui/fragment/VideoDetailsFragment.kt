package dev.kuylar.lighttube.ui.fragment

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import dev.kuylar.lighttube.api.models.LightTubeVideo
import dev.kuylar.lighttube.databinding.FragmentVideoDetailsBinding

class VideoDetailsFragment(private val video: LightTubeVideo) : Fragment() {
	private lateinit var binding: FragmentVideoDetailsBinding

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentVideoDetailsBinding.inflate(inflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		binding.videoTitle.text = video.title
		binding.videoDescription.text = Html.fromHtml(video.description, Html.FROM_HTML_MODE_COMPACT)
		binding.likeCount.text = video.likeCount
		binding.dislikeContainer.visibility = View.GONE // todo: return youtube dislike
		binding.viewCount.text = video.viewCount
		val dateSplits = video.dateText.split(", ")
		binding.publishedDate.text = dateSplits[0]
		binding.publishedYear.text = dateSplits[1]

		binding.channelTitle.text = video.channel.title
		binding.channelSubscribers.text = video.channel.subscribers
		Glide
			.with(this)
			.load(video.channel.avatar)
			.into(binding.channelAvatar)
		//todo: channel buttons
	}
}