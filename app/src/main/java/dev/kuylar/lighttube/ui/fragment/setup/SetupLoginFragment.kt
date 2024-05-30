package dev.kuylar.lighttube.ui.fragment.setup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.databinding.FragmentSetupLoginBinding
import dev.kuylar.lighttube.ui.activity.LoginActivity
import dev.kuylar.lighttube.ui.activity.SetupActivity

class SetupLoginFragment : Fragment() {
	private lateinit var binding: FragmentSetupLoginBinding

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentSetupLoginBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val sp = requireContext().getSharedPreferences("main", AppCompatActivity.MODE_PRIVATE)
		binding.buttonBack.setOnClickListener {
			(activity as SetupActivity).goToStage(
				if (sp.getBoolean("customInstance", false))
					SetupActivity.STAGE_INSTANCES_CUSTOM
				else
					SetupActivity.STAGE_INSTANCES_PUBLIC
			)
		}
		binding.buttonSkip.setOnClickListener {
			(activity as SetupActivity).goToStage(SetupActivity.STAGE_FINISH)
		}
		val intentLauncher =
			registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
				if (result.resultCode == Activity.RESULT_OK) {
					(activity as SetupActivity).goToStage(SetupActivity.STAGE_FINISH)
				}
			}
		binding.body.text = getString(R.string.setup_login_body, sp.getString("instanceHost", ""))
		binding.buttonRegister.setOnClickListener {
			val i = Intent(context, LoginActivity::class.java)
			i.putExtra("register", true)
			intentLauncher.launch(i)
		}
		binding.buttonLogin.setOnClickListener {
			val i = Intent(context, LoginActivity::class.java)
			i.putExtra("register", false)
			intentLauncher.launch(i)
		}
	}
}