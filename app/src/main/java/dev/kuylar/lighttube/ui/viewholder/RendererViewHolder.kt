package dev.kuylar.lighttube.ui.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import dev.kuylar.lighttube.api.models.UserData
import dev.kuylar.lighttube.api.models.renderers.RendererContainer

open class RendererViewHolder(view: View): RecyclerView.ViewHolder(view) {
	open fun bind(renderer: RendererContainer, userData: UserData?) {
		throw UnsupportedOperationException("Cannot call bind() on RendererViewHolder")
	}
}