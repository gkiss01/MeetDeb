package com.gkiss01.meetdeb.screens.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.gkiss01.meetdeb.ActivityViewModel
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.isAdmin
import com.gkiss01.meetdeb.network.Status
import com.mikepenz.materialdrawer.holder.DimenHolder
import com.mikepenz.materialdrawer.holder.ImageHolder
import com.mikepenz.materialdrawer.holder.StringHolder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.mikepenz.materialdrawer.model.SectionDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.iconDrawable
import com.mikepenz.materialdrawer.widget.AccountHeaderView
import com.mikepenz.materialdrawer.widget.MaterialDrawerSliderView
import kotlinx.android.synthetic.main.fragment_profile.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private val viewModelActivityKoin: ActivityViewModel by sharedViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val accountHeaderView = createSliderHeader(pf_slider)
        addSliderItems(pf_slider, 2)
        addSliderNavigation()

        viewModelActivityKoin.activeUser.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    accountHeaderView.currentProfileName.text = it.data?.name
                    accountHeaderView.currentProfileEmail.text = it.data?.email

                    pf_name.text = it.data?.name
                    pf_email.text = it.data?.email

                    pf_id.editText?.setText(String.format("%07d", it.data?.id), TextView.BufferType.NORMAL)

                    if (it.data?.isAdmin() == true) {
                        val color = ContextCompat.getColor(requireContext(), R.color.yellow)

                        pf_rank.text = getString(R.string.profile_admin)
                        pf_rank.setTextColor(color)
                        pf_profileImage.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.yellow)
                    }
                    else pf_rank.text = getString(R.string.profile_user)
                }
                Status.PENDING -> findNavController().navigate(R.id.registerFragment)
                else -> Log.e("MeetDebLog_ProfileFragment", "User is null...")
            }
        })

        viewModelActivityKoin.getEventsSummary().observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    pf_createdEvents.text = it.data?.eventsCreated.toString()
                    pf_acceptedEvents.text = it.data?.eventsInvolved.toString()
                }
                Status.ERROR -> Toast.makeText(requireContext(), it.errorMessage, Toast.LENGTH_LONG).show()
                Status.LOADING -> Log.d("MeetDebLog_ProfileFragment", "Loading events summary...")
                else -> {}
            }
        })

        pf_deleteLabel.setOnClickListener { findNavController().navigate(R.id.deleteBottomSheetFragment) }
        pf_updateLabel.setOnClickListener { findNavController().navigate(R.id.updateBottomSheetFragment) }
    }

    private fun addSliderNavigation() {
        pf_slider.onDrawerItemClickListener = { _, item, _ ->
            when (item.identifier) {
                1L -> findNavController().navigate(R.id.eventsFragment)
                3L -> {
                    viewModelActivityKoin.resetUserCredentials()
                    viewModelActivityKoin.resetLiveData()
                }
            }
            false
        }
    }
}

fun Fragment.createSliderHeader(slider: MaterialDrawerSliderView) = AccountHeaderView(this.requireContext()).apply {
    attachToSliderView(slider)
    height = DimenHolder.fromDp(200)
    headerBackground = ImageHolder(R.drawable.landscape)
    addProfile(ProfileDrawerItem(), 0)
    selectionListEnabledForSingleProfile = false
}

fun Fragment.addSliderItems(slider: MaterialDrawerSliderView, active: Int) {
    slider.closeOnClick = true
    slider.itemAdapter.add(
        PrimaryDrawerItem().apply {
            identifier = 1
            name = StringHolder(getString(R.string.drawer_events))
            iconDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_event)!!
            if (active == 1) {
                isSelected = true
                isEnabled = false
            }
        },
        PrimaryDrawerItem().apply {
            identifier = 2
            name = StringHolder(getString(R.string.drawer_profile))
            iconDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_person)!!
            if (active == 2) {
                isSelected = true
                isEnabled = false
            }
        },
        SectionDrawerItem().apply {
            name = StringHolder(getString(R.string.drawer_more))
        },
        SecondaryDrawerItem().apply {
            identifier = 3
            name = StringHolder(getString(R.string.drawer_logout))
            iconDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_logout)!!
        }
    )
}