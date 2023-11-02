package dev.kuylar.lighttube.ui.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import dev.kuylar.lighttube.api.models.UserData

open class RendererViewHolder(view: View): RecyclerView.ViewHolder(view) {
	open fun bind(item: JsonObject, userData: UserData?) {
		throw UnsupportedOperationException("Cannot call bind() on RendererViewHolder")
	}
}