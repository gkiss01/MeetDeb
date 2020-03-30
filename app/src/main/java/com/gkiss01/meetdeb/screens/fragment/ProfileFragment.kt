package com.gkiss01.meetdeb.screens.fragment

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.gkiss01.meetdeb.ActivityViewModel
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.utils.isActiveUserAdmin
import com.gkiss01.meetdeb.utils.setSavedUser
import com.mikepenz.materialdrawer.holder.DimenHolder
import com.mikepenz.materialdrawer.holder.ImageHolder
import com.mikepenz.materialdrawer.holder.StringHolder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.mikepenz.materialdrawer.model.SectionDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.iconDrawable
import com.mikepenz.materialdrawer.widget.AccountHeaderView
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private val activityViewModel: ActivityViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val accountHeaderView = createAccountHeader()
        addSliderItems()
        addSliderNavigation()

        activityViewModel.activeUser.observe(viewLifecycleOwner, Observer {
            pf_name.text = it.name
            pf_email.text = it.email

            pf_createdEvents.text = "137"
            pf_acceptedEvents.text = "457"
            pf_id.setText(String.format("%07d", it.id), TextView.BufferType.NORMAL)

            if (isActiveUserAdmin(it)) {
                val color = ContextCompat.getColor(context!!, R.color.yellow)

                pf_rank.text = getString(R.string.profile_admin)
                pf_rank.setTextColor(color)
                pf_profileImage.backgroundTintList = ContextCompat.getColorStateList(context!!, R.color.yellow)
            } else pf_rank.text = getString(R.string.profile_user)

            accountHeaderView.currentProfileName.text = it.name
            accountHeaderView.currentProfileEmail.text = it.email
        })

        pf_deleteLabel.setOnClickListener { findNavController().navigate(R.id.deleteBottomSheetFragment) }
        pf_updateLabel.setOnClickListener { findNavController().navigate(R.id.updateBottomSheetFragment) }
    }

    private fun createAccountHeader(): AccountHeaderView {
        return AccountHeaderView(context!!).apply {
            attachToSliderView(pf_slider)
            height = DimenHolder.fromDp(200)
            headerBackground = ImageHolder(R.drawable.landscape)
            addProfile(ProfileDrawerItem(), 0)
            selectionListEnabledForSingleProfile = false
        }
    }

    private fun addSliderItems() {
        pf_slider.itemAdapter.add(
            PrimaryDrawerItem().apply {
                identifier = 1
                name = StringHolder(getString(R.string.drawer_events))
                iconDrawable = ContextCompat.getDrawable(context!!, R.drawable.ic_event)!!
            },
            PrimaryDrawerItem().apply {
                identifier = 2
                name = StringHolder(getString(R.string.drawer_profile))
                isSelected = true
                isEnabled = false
                iconDrawable = ContextCompat.getDrawable(context!!, R.drawable.ic_person)!!
            },
            SectionDrawerItem().apply {
                name = StringHolder(getString(R.string.drawer_more))
            },
            SecondaryDrawerItem().apply {
                identifier = 3
                name = StringHolder(getString(R.string.drawer_logout))
                iconDrawable = ContextCompat.getDrawable(context!!, R.drawable.ic_logout)!!
            }
        )
    }

    private fun addSliderNavigation() {
        pf_slider.closeOnClick = true
        pf_slider.onDrawerItemClickListener = { _, item, _ ->
            when (item.identifier) {
                1L -> {
                    findNavController().navigate(R.id.eventsFragment)
                }
                3L -> {
                    setSavedUser(context!!, "", "")
                    activityViewModel.activeUser.value = null
                    activityViewModel.tempPassword = null
                    activityViewModel.password = ""
                    activityViewModel.basic = ""
                    findNavController().navigate(R.id.registerFragment)
                }
            }
            false
        }
    }
}