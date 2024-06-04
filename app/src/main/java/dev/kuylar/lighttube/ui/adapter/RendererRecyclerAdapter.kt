package dev.kuylar.lighttube.ui.adapter

import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import dev.kuylar.lighttube.Utils
import dev.kuylar.lighttube.api.models.UserData
import dev.kuylar.lighttube.ui.viewholder.RendererViewHolder

class RendererRecyclerAdapter(
	private val rendererList: MutableList<JsonObject>,
	private val requestMore: ((String) -> Unit)? = null
) : RecyclerView.Adapter<RendererViewHolder>() {

	private var userData = UserData(null, hashMapOf(), false, null)
	private var uiIsLandscape = false
	private val portraitOnlyRenderers =
		arrayOf("slimVideoInfoRenderer", "playlistInfoRenderer", "channelInfoRenderer")

	// forgive me other android devs that obviously
	// know better than me, but i had to do this :(
	override fun getItemViewType(position: Int): Int {
		return position
	}

	override fun onCreateViewHolder(parent: ViewGroup, position: Int): RendererViewHolder {
		val inflater = LayoutInflater.from(parent.context)
		return Utils.getViewHolder(
			rendererList[position], inflater!!, parent,
			parent.context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
		)
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

			"richItemRenderer" -> holder.bind(renderer.getAsJsonObject("content"), userData)
			else -> holder.bind(renderer, userData)
		}

		if (portraitOnlyRenderers.contains(type)) {
			holder.itemView.layoutParams = RecyclerView.LayoutParams(
				RecyclerView.LayoutParams.MATCH_PARENT,
				if (uiIsLandscape) 0 else RecyclerView.LayoutParams.WRAP_CONTENT
			)
		}
	}

	fun updateUserData(newUserData: UserData?) {
		if (newUserData == null) return
		userData.user = newUserData.user
		userData.channels.putAll(newUserData.channels)
		userData.editable = newUserData.editable
		userData.playlistId = newUserData.playlistId
	}

	fun notifyScreenRotated(isLandscape: Boolean) {
		uiIsLandscape = isLandscape
		if (rendererList.size == 0) return
		val firstElType = rendererList[0].get("type")
			.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }?.asString
		if (portraitOnlyRenderers.contains(firstElType))
			notifyItemChanged(0)
	}
}