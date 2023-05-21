package dev.kuylar.lighttube.ui.adapter

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.bumptech.glide.Glide
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.api.models.SubscriptionFeedItem
import dev.kuylar.lighttube.databinding.ItemSubscriptionVideoBinding
import java.text.DecimalFormat

class SubscriptionFeedRecyclerAdapter(
	val items: MutableList<SubscriptionFeedItem>,
	val playVideoCallback: ((String) -> Unit)) :
Adapter<SubscriptionFeedRecyclerAdapter.ViewHolder>() {
	private lateinit var binding: ItemSubscriptionVideoBinding
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val inflater = LayoutInflater.from(parent.context)
		binding = ItemSubscriptionVideoBinding.inflate(inflater, parent, false)
		return ViewHolder(binding, playVideoCallback)
	}

	override fun getItemCount(): Int {
		return items.size
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bind(items[position])
	}

	class ViewHolder(
		private val binding: ItemSubscriptionVideoBinding,
		private val playVideoCallback: (String) -> Unit
	) :
		RecyclerView.ViewHolder(binding.root) {
		fun bind(item: SubscriptionFeedItem) {
			val c = binding.root.context
			binding.videoTitle.text = item.title
			binding.videoSubtitle.text = c.getString(
				R.string.template_feed_video_subtitle,
				item.channelName,
				DecimalFormat().format(item.viewCount),
				DateUtils.getRelativeTimeSpanString(
					item.publishedDate.time,
					System.currentTimeMillis(),
					DateUtils.MINUTE_IN_MILLIS
				)
			)
			Glide
				.with(binding.root)
				.load(item.thumbnail)
				.into(binding.videoThumbnail)
			binding.root.setOnClickListener {
				playVideoCallback.invoke(item.id)
			}
		}
	}
}