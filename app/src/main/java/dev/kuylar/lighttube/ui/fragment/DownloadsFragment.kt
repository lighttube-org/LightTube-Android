package dev.kuylar.lighttube.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import dev.kuylar.lighttube.databinding.FragmentDownloadsBinding
import dev.kuylar.lighttube.downloads.VideoDownloadManager
import dev.kuylar.lighttube.ui.activity.MainActivity
import dev.kuylar.lighttube.ui.adapter.DownloadsRecyclerAdapter

@UnstableApi
class DownloadsFragment : Fragment() {
	private lateinit var binding: FragmentDownloadsBinding

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentDownloadsBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.recycler.layoutManager =
			LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
		binding.recycler.adapter = DownloadsRecyclerAdapter(requireActivity() as MainActivity)

		binding.pause.setOnClickListener {
			VideoDownloadManager.pauseAllDownloads(requireContext())
		}
		binding.resume.setOnClickListener {
			VideoDownloadManager.resumeAllDownloads(requireContext())
		}
	}
}