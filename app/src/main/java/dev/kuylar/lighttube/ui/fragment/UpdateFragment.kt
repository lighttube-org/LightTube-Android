package dev.kuylar.lighttube.ui.fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.UpdateInfo
import dev.kuylar.lighttube.databinding.FragmentUpdateBinding


class UpdateFragment(private val update: UpdateInfo) : BottomSheetDialogFragment() {
	private lateinit var binding: FragmentUpdateBinding

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentUpdateBinding.inflate(inflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		binding.updateSubtitle.text =
			getString(R.string.update_subtitle, update.latestVersion)
		binding.updateSkip.setOnClickListener {
			val sp = context?.getSharedPreferences("main", Context.MODE_PRIVATE)
			sp?.edit {
				putString("skippedUpdate", update.latestVersion)
			}
			Toast.makeText(context, R.string.update_skip_notice, Toast.LENGTH_LONG).show()
			dismiss()
		}
		binding.updateDownload.setOnClickListener {
			startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(update.downloadUrl)))
		}
	}
}