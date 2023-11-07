package dev.kuylar.lighttube.ui.viewholder

import android.text.Html
import android.view.View
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.api.models.UserData
import dev.kuylar.lighttube.databinding.RendererCommentBinding

class CommentRenderer(val binding: RendererCommentBinding) : RendererViewHolder(binding.root) {
	override fun bind(item: JsonObject, userData: UserData?) {
		if (item.getAsJsonPrimitive("pinned").asBoolean) {
			binding.commentPinned.visibility = View.VISIBLE
		}
		if (item.getAsJsonObject("owner").getAsJsonArray("badges").size() > 0) {
			binding.commentAuthor.setCompoundDrawables(
				null,
				null,
				ContextCompat.getDrawable(binding.root.context, R.drawable.ic_verified),
				null
			)
		}

		binding.commentAuthor.text =
			item.getAsJsonObject("owner").getAsJsonPrimitive("title").asString
		binding.commentDate.text =
			item.getAsJsonPrimitive("publishedTimeText").asString
		binding.commentBody.text =
			Html.fromHtml(
				item.getAsJsonPrimitive("content").asString,
				Html.FROM_HTML_MODE_LEGACY
			)

		val lc = item.get("likeCount")
		if (!lc.isJsonNull)
			binding.commentLikeCount.text =
				lc.asString

		Glide
			.with(binding.root)
			.load(item.getAsJsonObject("owner").asJsonObject.getAsJsonPrimitive("avatar").asString)
			.into(binding.commentAvatar)
	}
}