package dev.kuylar.lighttube.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.kuylar.lighttube.api.models.SubscriptionInfo
import dev.kuylar.lighttube.databinding.FragmentManageSubscriptionBinding
import dev.kuylar.lighttube.ui.activity.MainActivity
import kotlin.concurrent.thread

class ManageSubscriptionFragment(
	private val channelId: String,
	private val currentState: SubscriptionInfo,
	private val afterUpdate: (SubscriptionInfo) -> Unit
) : BottomSheetDialogFragment() {
	private lateinit var binding: FragmentManageSubscriptionBinding

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
	): View {
		binding = FragmentManageSubscriptionBinding.inflate(inflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		if (currentState.subscribed) {
			if (currentState.notifications) {
				binding.subscriptionNotificationsOnCheck.visibility = View.VISIBLE
			} else {
				binding.subscriptionNotificationsOffCheck.visibility = View.VISIBLE
			}
		} else {
			binding.subscriptionNotificationsUnsubscribeCheck.visibility = View.VISIBLE
		}

		binding.subscriptionNotificationsOn.setOnClickListener {
			updateSubscription(subscribed = true, notifications = true)
		}

		binding.subscriptionNotificationsOff.setOnClickListener {
			updateSubscription(subscribed = true, notifications = false)
		}

		binding.subscriptionNotificationsUnsubscribe.setOnClickListener {
			updateSubscription(subscribed = false, notifications = false)
		}
	}

	private fun updateSubscription(subscribed: Boolean, notifications: Boolean) {
		binding.subscriptionNotificationsOn.isEnabled = false
		binding.subscriptionNotificationsOff.isEnabled = false
		binding.subscriptionNotificationsUnsubscribe.isEnabled = false

		with((activity as MainActivity)) {
			thread {
				getApi().subscribe(channelId, subscribed, notifications)
				runOnUiThread {
					afterUpdate(SubscriptionInfo(subscribed, notifications))
					dismiss()
				}
			}
		}
	}
}