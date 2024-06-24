package dev.kuylar.lighttube.ui.fragment

import android.icu.text.DecimalFormat
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.Utils
import dev.kuylar.lighttube.api.models.LightTubeVideo
import dev.kuylar.lighttube.databinding.FragmentVideoDetailsBinding
import java.util.Locale

class VideoDetailsFragment : Fragment() {
	private lateinit var video: LightTubeVideo
	private lateinit var binding: FragmentVideoDetailsBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		arguments?.let {
			video = Utils.gson.fromJson(it.getString("video"), LightTubeVideo::class.java)
		}
	}

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
		binding.likeCount.text = Utils.toShortInt(requireContext(), video.likeCount)
		binding.dislikeContainer.visibility = View.GONE // todo: return youtube dislike
		binding.viewCount.text = DecimalFormat.getInstance().format(video.viewCount)

		val cal = Calendar.getInstance()
		cal.time = video.publishDate
		binding.publishedDate.text = SimpleDateFormat("MMM dd", Locale.getDefault()).format(video.publishDate)
		binding.publishedYear.text = cal.get(Calendar.YEAR).toString()

		binding.channelTitle.text = video.channel.title
		binding.channelSubscribers.text = resources.getQuantityString(
			R.plurals.template_subscribers,
			video.channel.subscribers ?: 0,
			Utils.toShortInt(requireContext(), video.channel.subscribers?.toLong() ?: 0L)
		)
		Glide
			.with(this)
			.load(Utils.getBestImageUrl(video.channel.avatar))
			.into(binding.channelAvatar)
		//todo: channel buttons
	}
}