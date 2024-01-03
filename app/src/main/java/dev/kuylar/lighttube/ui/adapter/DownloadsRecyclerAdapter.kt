package dev.kuylar.lighttube.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.Utils
import dev.kuylar.lighttube.databinding.ItemDownloadBinding
import dev.kuylar.lighttube.downloads.DownloadInfo
import dev.kuylar.lighttube.downloads.VideoDownloadManager
import dev.kuylar.lighttube.ui.VideoPlayerManager
import dev.kuylar.lighttube.ui.activity.MainActivity
import kotlin.math.roundToInt

@UnstableApi
class DownloadsRecyclerAdapter(private val activity: MainActivity) :
	RecyclerView.Adapter<DownloadsRecyclerAdapter.ViewHolder>() {
	private val layoutInflater = activity.layoutInflater

	// todo: https://github.com/lighttube-org/LightTube-Android/pull/24
	private val videoPlayerManager = activity.player

	private var downloads = emptyList<DownloadInfo>()

	init {
		refresh()
	}

	class ViewHolder(
		private val binding: ItemDownloadBinding,
		private val videoPlayerManager: VideoPlayerManager,
		private val adapter: DownloadsRecyclerAdapter
	) : RecyclerView.ViewHolder(binding.root) {
		var startedUpdating = false

		fun bind(video: DownloadInfo, updated: Boolean = false) {
			binding.videoTitle.text = video.title
			binding.videoDuration.visibility = View.GONE

			if (!updated) {
				Glide.with(binding.root)
					.load("https://i.ytimg.com/vi/${video.id}/hqdefault.jpg") // todo: download this too
					.into(binding.videoThumbnail)
				Glide.with(binding.root)
					.load(video.channelAvatar) // todo: download this too
					.into(binding.channelAvatar)
			}

			val items = ArrayList<String>()
			items.add(video.channelName)
			items.add(Utils.humanReadableByteCount(video.size))
			// For some reason, while the download is ongoing, the
			// download state is STATE_QUEUED
			items.add(binding.root.resources.getStringArray(R.array.download_state)[if (video.state == 0) 2 else video.state])
			binding.videoSubtitle.text = items.joinToString(" • ")

			when (video.progress) {
				0f -> binding.downloadProgress.isIndeterminate = true
				100f -> binding.downloadProgress.visibility = View.GONE
				else -> binding.downloadProgress.setProgressCompat(
					video.progress.roundToInt(),
					updated
				)
			}

			when (video.state) {
				Download.STATE_QUEUED -> {
					binding.settingsButton.setIconResource(R.drawable.ic_delete)
					binding.settingsButton.setOnClickListener {
						remove(binding.root.context, video)
					}
				}

				Download.STATE_STOPPED -> {
					binding.settingsButton.setIconResource(R.drawable.ic_play)
					binding.settingsButton.setOnClickListener {
						VideoDownloadManager.startDownload(binding.root.context, video.id)
					}
					binding.settingsButton.setOnLongClickListener {
						remove(binding.root.context, video)
						true
					}
				}

				Download.STATE_DOWNLOADING -> {
					binding.settingsButton.setIconResource(R.drawable.ic_pause)
					binding.settingsButton.setOnClickListener {
						Toast.makeText(
							binding.root.context,
							R.string.download_long_press_to_remove,
							Toast.LENGTH_LONG
						).show()
						VideoDownloadManager.stopDownload(binding.root.context, video.id, 1)
					}
					binding.settingsButton.setOnLongClickListener {
						remove(binding.root.context, video)
						true
					}
				}

				Download.STATE_COMPLETED -> {
					binding.settingsButton.setIconResource(R.drawable.ic_delete)
					binding.settingsButton.setOnClickListener {
						remove(binding.root.context, video)
					}
				}

				Download.STATE_FAILED -> {
					binding.settingsButton.visibility = View.GONE
					binding.downloadProgress.visibility = View.GONE
				}
				Download.STATE_REMOVING -> {
					binding.settingsButton.visibility = View.GONE
					binding.downloadProgress.visibility = View.GONE
				}
				Download.STATE_RESTARTING -> {
					binding.settingsButton.visibility = View.GONE
					binding.downloadProgress.visibility = View.GONE
				}
				else -> {
					binding.settingsButton.visibility = View.GONE
					binding.downloadProgress.visibility = View.GONE
				}
			}

			binding.root.setOnClickListener {
				Toast.makeText(binding.root.context, "play dl video", Toast.LENGTH_LONG).show()
				//videoPlayerManager.playVideo(video.id)
			}
			if (!updated)
				update(binding.root.context, video)
		}

		private fun remove(context: Context, video: DownloadInfo) {
			MaterialAlertDialogBuilder(context).apply {
				setTitle(R.string.download_delete_title)
				setTitle(R.string.download_delete_body)
				setNegativeButton(R.string.action_cancel) { dialog, _ -> dialog.dismiss() }
				setPositiveButton(R.string.action_delete) { dialog, _ ->
					VideoDownloadManager.removeDownload(context, video.id)
					dialog.dismiss()
				}
			}.create().show()
		}

		private fun update(context: Context, video: DownloadInfo) {
			if (startedUpdating) return
			startedUpdating = true
			Handler(context.mainLooper).postDelayed({
				updateLoop(context, video)
			}, DOWNLOAD_POLL_RATE)
		}

		private fun updateLoop(context: Context, video: DownloadInfo) {
			val info = VideoDownloadManager.getDownload(context, video.id)
			if (info == null) {
				adapter.refresh()
				return
			}
			try {
				bind(video, true)

				if (binding.root.drawingTime != 0L)
					Handler(context.mainLooper).postDelayed({
						updateLoop(context, video)
					}, DOWNLOAD_POLL_RATE)
			} catch (_: Exception) {

			}
		}

		companion object {
			const val DOWNLOAD_POLL_RATE = 1000L
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
		ItemDownloadBinding.inflate(layoutInflater, parent, false),
		videoPlayerManager,
		this
	)

	override fun getItemCount() = downloads.count()

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bind(downloads[position])
	}

	@SuppressLint("NotifyDataSetChanged")
	fun refresh() {
		downloads = VideoDownloadManager.getDownloads(activity)
		this.notifyDataSetChanged()
	}
}