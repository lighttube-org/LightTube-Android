package dev.kuylar.lighttube.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import dev.kuylar.lighttube.Utils
import dev.kuylar.lighttube.ui.viewholder.RendererViewHolder

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
		return Utils.getViewHolder(rendererList[position], inflater!!, parent)
	}

	override fun getItemCount(): Int = rendererList.size

	override fun onBindViewHolder(holder: RendererViewHolder, position: Int) {
		val renderer = rendererList[position]
		val type = renderer.getAsJsonPrimitive("type").asString
		when (type) {
			"continuationItemRenderer" -> {
				val token = renderer.get("token")
				if (!token.isJsonNull)
					requestMore?.invoke(token.asString)
			}

			"richItemRenderer" -> holder.bind(renderer.getAsJsonObject("content"))
			else -> holder.bind(renderer)
		}
	}
}