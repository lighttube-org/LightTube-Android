package dev.kuylar.lighttube.ui.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject

open class RendererViewHolder(view: View): RecyclerView.ViewHolder(view) {
	open fun bind(item: JsonObject) {
		throw UnsupportedOperationException("Cannot call bind() on RendererViewHolder")
	}
}