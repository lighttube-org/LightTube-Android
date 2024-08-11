package dev.kuylar.lighttube.ui.adapter

import android.content.res.Configuration
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.updateMargins
import androidx.recyclerview.widget.RecyclerView
import dev.kuylar.lighttube.Utils
import dev.kuylar.lighttube.api.models.UserData
import dev.kuylar.lighttube.api.models.renderers.ContinuationRendererData
import dev.kuylar.lighttube.api.models.renderers.RendererContainer
import dev.kuylar.lighttube.ui.viewholder.RendererViewHolder

class RendererRecyclerAdapter(
	private val rendererList: MutableList<RendererContainer>,
	private val requestMore: ((String) -> Unit)? = null
) : RecyclerView.Adapter<RendererViewHolder>() {

	private var userData = UserData(null, hashMapOf(), false, null)
	private var uiIsLandscape = false
	private val portraitOnlyRenderers =
		arrayOf("slimVideoInfoRenderer", "playlistInfoRenderer", "channelInfoRenderer")

	private val flags = ArrayList<String>()
	private var expandable = false
	private var collapsedItemCount = 1
	var isExpanded = true
		private set

	// forgive me other android devs that obviously
	// know better than me, but i had to do this :(
	override fun getItemViewType(position: Int): Int {
		return position
	}

	override fun onCreateViewHolder(parent: ViewGroup, position: Int): RendererViewHolder {
		val inflater = LayoutInflater.from(parent.context)
		return Utils.getViewHolder(
			rendererList[position], inflater!!, parent,
			 if (flags.contains("forcePortrait")) false else parent.context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
		)
	}

	override fun getItemCount(): Int {
		return if (expandable && !isExpanded)
			rendererList.size.coerceAtMost(collapsedItemCount)
		else
			rendererList.size
	}

	// todo: try/catch this
	override fun onBindViewHolder(holder: RendererViewHolder, position: Int) {
		val renderer = rendererList[position]
		val type = renderer.type
		when (type) {
			"continuation" -> {
				requestMore?.invoke((renderer.data as ContinuationRendererData).continuationToken)
			}

//			"richItemRenderer" -> holder.bind(renderer.getAsJsonObject("content"), userData)
			else -> holder.bind(renderer, userData)
		}

		if (portraitOnlyRenderers.contains(type)) {
			holder.itemView.layoutParams = RecyclerView.LayoutParams(
				RecyclerView.LayoutParams.MATCH_PARENT,
				if (uiIsLandscape) 0 else RecyclerView.LayoutParams.WRAP_CONTENT
			)
		}

		if (flags.contains("smaller")) {
			val res = holder.itemView.context.resources
			val p = RecyclerView.LayoutParams(
				Math.round(res.displayMetrics.widthPixels * .8).toInt().coerceAtMost(
					TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 240f, res.displayMetrics)
						.toInt()
				),
				holder.itemView.layoutParams.height
			)
			p.updateMargins(
				TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, res.displayMetrics)
					.toInt(),
				0,
				TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, res.displayMetrics)
					.toInt(),
				TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, res.displayMetrics)
					.toInt()
			)
			holder.itemView.layoutParams = p;
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
		val firstElType = rendererList[0].type
		if (portraitOnlyRenderers.contains(firstElType))
			notifyItemChanged(0)
	}

	fun setExpandable(collapsedItemCount: Int) {
		this.collapsedItemCount = collapsedItemCount
		expandable = true
		isExpanded = false
	}

	fun setExpanded(expanded: Boolean) {
		if (isExpanded && !expanded)
			notifyItemRangeRemoved(collapsedItemCount, rendererList.size - collapsedItemCount)
		else if (!isExpanded && expanded)
			notifyItemRangeInserted(collapsedItemCount, rendererList.size - collapsedItemCount)
		isExpanded = expanded
	}

	fun setFlag(flag: String) {
		flags.add(flag)
	}
}