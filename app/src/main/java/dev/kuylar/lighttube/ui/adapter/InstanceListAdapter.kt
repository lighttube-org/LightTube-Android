package dev.kuylar.lighttube.ui.adapter

import android.app.Activity
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.kuylar.lighttube.api.LightTubeInstance
import dev.kuylar.lighttube.databinding.ItemInstanceBinding
import kotlin.concurrent.thread

class InstanceListAdapter(
	private val activity: Activity,
	private val items: List<LightTubeInstance>,
	private val onClick: (LightTubeInstance) -> Unit
) : RecyclerView.Adapter<InstanceListAdapter.InstanceViewHolder>() {
	class InstanceViewHolder(private val binding: ItemInstanceBinding) :
		RecyclerView.ViewHolder(binding.root) {
		fun bind(instance: LightTubeInstance, onClick: (LightTubeInstance) -> Unit) {
			thread {
				instance.fillBinding(binding, (binding.root.context as Activity))
			}
			binding.root.setOnClickListener {
				onClick.invoke(instance)
			}
		}
	}

	override fun getItemCount() = items.size

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
		InstanceViewHolder(ItemInstanceBinding.inflate(activity.layoutInflater, parent, false))

	override fun onBindViewHolder(holder: InstanceViewHolder, position: Int) {
		holder.bind(items[position], onClick)
	}
}