package dev.kuylar.lighttube.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.JsonObject
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.api.LightTubeApi
import dev.kuylar.lighttube.api.models.LightTubeException
import dev.kuylar.lighttube.api.models.LightTubeVideo
import dev.kuylar.lighttube.databinding.FragmentVideoInfoBinding
import dev.kuylar.lighttube.ui.activity.MainActivity
import dev.kuylar.lighttube.ui.adapter.RendererRecyclerAdapter
import java.io.IOException
import kotlin.concurrent.thread

class VideoInfoFragment : Fragment() {
	private val items: MutableList<JsonObject> = mutableListOf()
	private lateinit var id: String
	private var playlistId: String? = null
	private lateinit var detailsSheet: BottomSheetBehavior<LinearLayout>
	private lateinit var commentsSheet: BottomSheetBehavior<LinearLayout>
	private lateinit var binding: FragmentVideoInfoBinding
	private lateinit var api: LightTubeApi

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		api = (requireActivity() as MainActivity).api
		arguments?.let {
			id = it.getString("id")!!
			playlistId = it.getString("playlistId")
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
				try {
					val video = api.getVideo(id, playlistId)
					activity?.runOnUiThread {
						fillData(video.data!!)
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

		detailsSheet = BottomSheetBehavior.from(binding.sheetVideoDetails)
		commentsSheet = BottomSheetBehavior.from(binding.sheetComments)

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

		items.addAll(video.recommended)
		val adapter = RendererRecyclerAdapter(items)
		binding.recyclerRecommended.layoutManager = LinearLayoutManager(context)
		binding.recyclerRecommended.adapter = adapter

		requireActivity().supportFragmentManager.beginTransaction().apply {
			replace(R.id.video_info_fragment, VideoDetailsFragment::class.java, bundleOf(Pair("video", Gson().toJson(video))))

			replace(R.id.comments_fragment, VideoCommentsFragment::class.java, bundleOf(Pair("commentsContinuation", video.commentsContinuation)))
		}.commit()

		binding.videoDetails.setOnClickListener {
			detailsSheet.state = BottomSheetBehavior.STATE_EXPANDED
		}
		binding.sheetVideoDetailsClose.setOnClickListener {
			detailsSheet.state = BottomSheetBehavior.STATE_HIDDEN
		}

		binding.buttonComments.setOnClickListener {
			commentsSheet.state = BottomSheetBehavior.STATE_EXPANDED
		}
		binding.sheetCommentsClose.setOnClickListener {
			commentsSheet.state = BottomSheetBehavior.STATE_HIDDEN
		}

		binding.buttonShare.setOnClickListener {
			val sendIntent: Intent = Intent().apply {
				action = Intent.ACTION_SEND
				putExtra(Intent.EXTRA_TEXT, "https://youtu.be/${video.id}")
				type = "text/plain"
			}

			val shareIntent = Intent.createChooser(sendIntent, null)
			startActivity(shareIntent)
		}
	}

	fun closeSheets(): Boolean {
		if (commentsSheet.state != BottomSheetBehavior.STATE_HIDDEN) {
			commentsSheet.state = BottomSheetBehavior.STATE_HIDDEN
			return true
		}

		if (detailsSheet.state != BottomSheetBehavior.STATE_HIDDEN) {
			detailsSheet.state = BottomSheetBehavior.STATE_HIDDEN
			return true
		}

		return false
	}
}