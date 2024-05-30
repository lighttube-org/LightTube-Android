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

		if (item.has("owner")) item.get("owner").takeIf { !it.isJsonNull }?.asJsonObject?.apply {
			if (has("title") && !get("title").isJsonNull)
				binding.commentAuthor.text = getAsJsonPrimitive("title").asString
			if (!get("badges").isJsonNull && getAsJsonArray("badges").size() > 0)
				binding.commentAuthor.setCompoundDrawables(
					null,
					null,
					ContextCompat.getDrawable(binding.root.context, R.drawable.ic_verified),
					null
				)

			if (has("avatar") && !get("avatar").isJsonNull)
				Glide.with(binding.root)
					.load(getAsJsonPrimitive("avatar").asString)
					.into(binding.commentAvatar)
		}

		binding.commentDate.text =
			item.getAsJsonPrimitive("publishedTimeText").asString
		binding.commentBody.text =
			Html.fromHtml(
				item.getAsJsonPrimitive("content").asString,
				Html.FROM_HTML_MODE_LEGACY
			)

		val lc = item.get("likeCount")
		if (!lc.isJsonNull)
			binding.commentLikeCount.text = lc.asString
	}
}