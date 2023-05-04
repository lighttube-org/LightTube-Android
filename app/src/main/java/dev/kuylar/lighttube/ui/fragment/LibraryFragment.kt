package dev.kuylar.lighttube.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dev.kuylar.lighttube.databinding.FragmentLibraryBinding

class LibraryFragment : Fragment() {
	private lateinit var binding: FragmentLibraryBinding

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentLibraryBinding.inflate(inflater)
		return binding.root
	}
}