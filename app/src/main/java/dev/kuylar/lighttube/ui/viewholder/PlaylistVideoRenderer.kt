package dev.kuylar.lighttube.ui.viewholder

import android.view.View
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.Utils
import dev.kuylar.lighttube.api.models.UserData
import dev.kuylar.lighttube.api.models.renderers.RendererContainer
import dev.kuylar.lighttube.api.models.renderers.VideoRendererData
import dev.kuylar.lighttube.databinding.RendererPlaylistVideoBinding
import dev.kuylar.lighttube.ui.activity.MainActivity
import kotlin.concurrent.thread

class PlaylistVideoRenderer(val binding: RendererPlaylistVideoBinding) :
	RendererViewHolder(binding.root) {
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
			.load(Utils.getBestImageUrl(item.thumbnails))
			.into(binding.videoThumbnail)

		if (item.author?.title != null)
			binding.videoSubtitle.text = item.author.title

		if (item.editable == true) {
			binding.buttonDeleteVideo.setOnClickListener {
				MaterialAlertDialogBuilder(binding.root.context).apply {
					setTitle(R.string.playlist_delete_video_title)
					setMessage(R.string.playlist_delete_video_body)
					setPositiveButton(R.string.action_delete) { _, _ ->
						thread {
							if (binding.root.context is MainActivity && userData!!.playlistId != null)
								(binding.root.context as MainActivity).getApi()
									.deleteVideoFromPlaylist(
										userData.playlistId!!,
										item.videoId
									)
						}
					}
					setNegativeButton(R.string.action_cancel) { dialog, _ ->
						dialog.dismiss()
					}
				}.show()
			}
		} else {
			binding.buttonDeleteVideo.visibility = View.GONE
		}

		binding.root.setOnClickListener {
			// bad idea? idk
			if (binding.root.context is MainActivity)
				(binding.root.context as MainActivity).getPlayer().playVideo(item.videoId)
			else
				Toast.makeText(binding.root.context, "uhh click :3", Toast.LENGTH_LONG).show()
		}
	}
}