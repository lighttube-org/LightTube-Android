package dev.kuylar.lighttube.ui.fragment.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dev.kuylar.lighttube.databinding.FragmentSetupWelcomeBinding
import dev.kuylar.lighttube.ui.activity.SetupActivity

class SetupWelcomeFragment : Fragment() {
	private lateinit var binding: FragmentSetupWelcomeBinding

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentSetupWelcomeBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		binding.nextButton.setOnClickListener {
			(activity as SetupActivity).goToStage(SetupActivity.STAGE_INSTANCES_PUBLIC)
		}
	}
}