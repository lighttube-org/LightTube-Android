package dev.kuylar.lighttube.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import dev.kuylar.lighttube.databinding.RendererCommentBinding
import dev.kuylar.lighttube.databinding.RendererContinuationBinding
import dev.kuylar.lighttube.databinding.RendererSlimVideoInfoBinding
import dev.kuylar.lighttube.databinding.RendererUnknownBinding
import dev.kuylar.lighttube.databinding.RendererVideoBinding
import dev.kuylar.lighttube.ui.viewholder.CommentRenderer
import dev.kuylar.lighttube.ui.viewholder.ContinuationRenderer
import dev.kuylar.lighttube.ui.viewholder.RendererViewHolder
import dev.kuylar.lighttube.ui.viewholder.SlimVideoInfoRenderer
import dev.kuylar.lighttube.ui.viewholder.UnknownRenderer
import dev.kuylar.lighttube.ui.viewholder.VideoRenderer

class RendererRecyclerAdapter(
	private val rendererList: MutableList<JsonObject>,
	private val requestMore: ((String) -> Unit)? = null
) :
	RecyclerView.Adapter<RendererViewHolder>() {

	// forgive me other android devs that obviously
	// know better than me, but i had to do this :(
	override fun getItemViewType(position: Int): Int {
		return position
	}

	override fun onCreateViewHolder(parent: ViewGroup, position: Int): RendererViewHolder {
		val inflater = LayoutInflater.from(parent.context)
		return when (rendererList[position].getAsJsonPrimitive("type").asString) {
			"videoRenderer" -> VideoRenderer(RendererVideoBinding.inflate(inflater, parent, false))
			"compactVideoRenderer" -> VideoRenderer(RendererVideoBinding.inflate(inflater, parent, false))
			"commentThreadRenderer" -> CommentRenderer(RendererCommentBinding.inflate(inflater, parent, false))
			"continuationItemRenderer" -> ContinuationRenderer(RendererContinuationBinding.inflate(inflater, parent, false))
			"slimVideoInfoRenderer" -> SlimVideoInfoRenderer(RendererSlimVideoInfoBinding.inflate(inflater, parent, false))
			else -> UnknownRenderer(RendererUnknownBinding.inflate(inflater, parent, false))
		}
	}

	override fun getItemCount(): Int = rendererList.size

	override fun onBindViewHolder(holder: RendererViewHolder, position: Int) {
		val renderer = rendererList[position]
		val type = renderer.getAsJsonPrimitive("type").asString
		if (type == "continuationItemRenderer") {
			val token = renderer.get("token")
			if (!token.isJsonNull)
				requestMore?.invoke(token.asString)
		}
		holder.bind(renderer)
	}
}