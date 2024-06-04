package dev.kuylar.lighttube.ui.fragment.setup

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.databinding.FragmentSetupFinishBinding
import dev.kuylar.lighttube.ui.activity.SetupActivity

class SetupFinishFragment : Fragment() {
	private lateinit var binding: FragmentSetupFinishBinding

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentSetupFinishBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		val sp = requireContext().getSharedPreferences("main", AppCompatActivity.MODE_PRIVATE)

		binding.host.text = sp.getString("instanceHost", "")
		binding.account.text = if (sp.contains("username"))
			Html.fromHtml(getString(R.string.setup_complete_account_loggedin, sp.getString("username", "")), Html.FROM_HTML_MODE_LEGACY)
		else
			getString(R.string.setup_complete_account_not_loggedin)

		binding.finish.setOnClickListener {
			(activity as SetupActivity).finishSetup()
		}
		binding.back.setOnClickListener {
			(activity as SetupActivity).goToStage(SetupActivity.STAGE_LOGIN)
		}
	}
}