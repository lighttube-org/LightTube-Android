package dev.kuylar.lighttube.ui.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.Utils
import dev.kuylar.lighttube.databinding.FragmentHomeBinding
import dev.kuylar.lighttube.ui.activity.MainActivity
import dev.kuylar.lighttube.ui.activity.SetupActivity
import java.io.IOException
import kotlin.concurrent.thread


class HomeFragment : Fragment() {
	private lateinit var binding: FragmentHomeBinding

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentHomeBinding.inflate(inflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		val a = (activity as MainActivity)
		val sp = a.getSharedPreferences("main", Activity.MODE_PRIVATE)
		binding.homeMotd.text = Utils.randomMotd(sp.getStringSet("cachedMotds", mutableSetOf("Search something to get started!"))!!.toList())

		a.setLoading(true)
		thread {
			try {
				val info = a.getApi().getInstanceInfo()
				a.runOnUiThread {
					a.setLoading(false)
					if (info.type != "lighttube/2.0") {
						MaterialAlertDialogBuilder(requireContext()).apply {
							setTitle(R.string.lighttube_3_title)
							setMessage(R.string.lighttube_3_body)
							setCancelable(false)
							setNegativeButton(R.string.lighttube_3_instance) { dialog, _ ->
								startActivity(Intent(requireContext(), SetupActivity::class.java))
							}
						}.show()
					}
					binding.homeMotd.text = Utils.randomMotd(info.motd)
					sp.edit {
						putStringSet("cachedMotds", info.motd.toMutableSet())
					}
				}
			} catch (e: IOException) {
				a.runOnUiThread {
					a.setLoading(false)
					val sb = Snackbar.make(binding.root, R.string.error_connection, Snackbar.LENGTH_INDEFINITE)
					sb.setAnchorView(activity?.findViewById(R.id.nav_view))
					sb.setAction(R.string.action_close) {
						sb.dismiss()
					}
					sb.show()
				}
			}
		}
	}
}