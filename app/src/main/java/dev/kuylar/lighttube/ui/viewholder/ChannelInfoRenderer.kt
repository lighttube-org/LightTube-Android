package dev.kuylar.lighttube.ui.viewholder

import com.google.gson.Gson
import com.google.gson.JsonObject
import dev.kuylar.lighttube.api.models.LightTubeChannel
import dev.kuylar.lighttube.api.models.UserData
import dev.kuylar.lighttube.databinding.RendererChannelInfoBinding

class ChannelInfoRenderer(private val binding: RendererChannelInfoBinding) :
	RendererViewHolder(binding.root) {
	override fun bind(item: JsonObject, userData: UserData?) {
		val channel = Gson().fromJson(item, LightTubeChannel::class.java)
		channel.fillBinding(binding.header, userData)
	}
}