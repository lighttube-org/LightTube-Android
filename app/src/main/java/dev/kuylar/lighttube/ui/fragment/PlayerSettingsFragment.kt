package dev.kuylar.lighttube.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Tracks
import com.google.android.exoplayer2.trackselection.TrackSelectionOverride
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.databinding.FragmentPlayerSettingsBinding
import dev.kuylar.lighttube.databinding.ItemPlayerSettingBinding


class PlayerSettingsFragment(
	private val player: Player,
	private val defaultScreen: String? = null
) : BottomSheetDialogFragment() {
	private lateinit var binding: FragmentPlayerSettingsBinding

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
	): View {
		binding = FragmentPlayerSettingsBinding.inflate(inflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		loadQualityMenu()
		loadSubtitleMenu()
		binding.playerSettingsLoopValue.text =
			if (player.repeatMode == Player.REPEAT_MODE_ONE) getString(R.string.on)
			else getString(R.string.off)

		binding.playerSettingsButtonCaption.setOnClickListener {
			binding.playerSettingsMain.visibility = View.GONE
			binding.playerSettingsCaption.visibility = View.VISIBLE
		}
		binding.playerSettingsButtonLoop.setOnClickListener {
			player.repeatMode =
				if (player.repeatMode == Player.REPEAT_MODE_ONE) Player.REPEAT_MODE_OFF else Player.REPEAT_MODE_ONE
			dismissNow()
		}
		binding.playerSettingsButtonSpeed.setOnClickListener {
			binding.playerSettingsMain.visibility = View.GONE
			binding.playerSettingsSpeed.visibility = View.VISIBLE
		}
		binding.playerSettingsButtonAudio.setOnClickListener {
			binding.playerSettingsMain.visibility = View.GONE
			binding.playerSettingsTrack.visibility = View.VISIBLE
		}

		when (defaultScreen) {
			"caption" -> {
				binding.playerSettingsMain.visibility = View.GONE
				binding.playerSettingsCaption.visibility = View.VISIBLE
			}
		}
	}

	private fun createMenuItem(
		label: String, checked: Boolean, onClick: ((View) -> Unit)
	): LinearLayout {
		val layout = ItemPlayerSettingBinding.inflate(layoutInflater)
		layout.playerSettingCheck.visibility = if (checked) View.VISIBLE else View.INVISIBLE
		layout.playerSettingLabel.text = label
		layout.root.setOnClickListener(onClick)
		return layout.root
	}

	@SuppressLint("SetTextI18n")
	private fun loadQualityMenu() {
		if (player.currentMediaItem?.mediaMetadata?.extras?.getString("fallback") != null && player.currentTracks.groups.size > 0) {
			binding.playerSettingsButtonQuality.setOnClickListener {
				binding.playerSettingsMain.visibility = View.GONE
				binding.playerSettingsQuality.visibility = View.VISIBLE
			}

			val group = player.currentTracks.groups[0]
			val isAuto =
				player.trackSelectionParameters.overrides.any { it.key.type == 0 } || multipleSelected(
					group
				)

			// Auto resolution
			binding.playerSettingsQuality.addView(createMenuItem(
				getString(R.string.player_quality_auto),
				isAuto
			) {
				player.trackSelectionParameters = player.trackSelectionParameters
					.buildUpon()
					.clearOverridesOfType(C.TRACK_TYPE_VIDEO)
					.build()
				dismissNow()
			})

			// Video resolutions
			for (i in 1 until group.length) {
				val fmt = group.getTrackFormat(i)
				if (fmt.sampleMimeType?.equals("video/avc") != false) continue
				val item = createMenuItem(
					"${fmt.height}p",
					if (isAuto) false else group.isTrackSelected(i)
				) {
					player.trackSelectionParameters = player.trackSelectionParameters
						.buildUpon()
						.setOverrideForType(
							TrackSelectionOverride(group.mediaTrackGroup, i)
						)
						.build()
					dismissNow()
				}
				binding.playerSettingsQuality.addView(item)
			}

			// Button label
			if (isAuto)
				binding.playerSettingsQualityValue.text = getString(R.string.player_quality_auto)
			else {
				try {
					val formatIndex =
						player.trackSelectionParameters.overrides[player.currentTracks.groups[0].mediaTrackGroup]!!.trackIndices.first()
					val format = player.currentTracks.groups[0].getTrackFormat(formatIndex)
					binding.playerSettingsQualityValue.text = "${format.height}p"
				} catch (_: Exception) {
					binding.playerSettingsQualityValue.text =
						getString(R.string.player_quality_auto)
				}
			}
		} else {
			binding.playerSettingsQualityValue.text = getString(R.string.unavailable)
		}
	}

	private fun multipleSelected(group: Tracks.Group): Boolean {
		var selected = 0
		for (i in 1 until group.length)
			if (group.isTrackSelected(i))
				selected++
		return selected > 1
	}

	private fun loadSubtitleMenu() {
		val textTracks = player.currentTracks.groups.filter { it.type == C.TRACK_TYPE_TEXT }
		if (textTracks.isNotEmpty()) {
			val params =
				player.trackSelectionParameters.overrides.filter { it.key.type == C.TRACK_TYPE_TEXT }

			binding.playerSettingsCaptionValue.text = if (params.isEmpty()) {
				getString(R.string.off)
			} else {
				val f =
					textTracks
						.find { it.mediaTrackGroup.id == params.values.first()!!.mediaTrackGroup.id }
						?.getTrackFormat(0)
				f?.label ?: f?.id ?: f?.language ?: getString(R.string.unavailable)
			}

			binding.playerSettingsCaption.addView(createMenuItem(
				getString(R.string.off),
				params.isEmpty()
			) {
				player.trackSelectionParameters = player.trackSelectionParameters
					.buildUpon()
					.clearOverridesOfType(C.TRACK_TYPE_TEXT)
					.build()
				dismissNow()
			})

			for (group in textTracks) {
				val f = group.getTrackFormat(0)
				val item = createMenuItem(
					f.label ?: f.id ?: f.language ?: getString(R.string.unavailable),
					if (params.isNotEmpty()) params.values.first()!!.mediaTrackGroup.id == group.mediaTrackGroup.id else false
				) {
					player.trackSelectionParameters = player.trackSelectionParameters
						.buildUpon()
						.clearOverridesOfType(C.TRACK_TYPE_TEXT)
						.setOverrideForType(
							TrackSelectionOverride(group.mediaTrackGroup, 0)
						)
						.build()
					dismissNow()
				}
				binding.playerSettingsCaption.addView(item)
			}
		} else {
			binding.playerSettingsButtonCaption.visibility = View.GONE
		}
	}
}
