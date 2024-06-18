package dev.kuylar.lighttube.ui.viewholder

import android.text.Html
import android.view.View
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.Utils
import dev.kuylar.lighttube.api.models.UserData
import dev.kuylar.lighttube.api.models.renderers.CommentRendererData
import dev.kuylar.lighttube.api.models.renderers.RendererContainer
import dev.kuylar.lighttube.databinding.RendererCommentBinding

class CommentRenderer(val binding: RendererCommentBinding) : RendererViewHolder(binding.root) {
	override fun bind(renderer: RendererContainer, userData: UserData?) {
		val item = renderer.data as CommentRendererData
		if (item.pinned)
			binding.commentPinned.visibility = View.VISIBLE

		binding.commentAuthor.text = item.owner.title
		if (item.owner.badges?.isNotEmpty() == true)
		binding.commentAuthor.setCompoundDrawables(
			null,
			null,
			ContextCompat.getDrawable(binding.root.context, R.drawable.ic_verified),
			null
		)
		if (item.owner.avatar != null)
			Glide.with(binding.root)
				.load(Utils.getBestImageUrl(item.owner.avatar))
				.into(binding.commentAvatar)

		binding.commentDate.text = item.publishedTimeText
		binding.commentBody.text = Html.fromHtml(item.content, Html.FROM_HTML_MODE_LEGACY)

		binding.commentLikeCount.text = item.likeCountText
	}
}