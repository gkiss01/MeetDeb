package com.gkiss01.meetdeb.screens

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.utils.getActiveUser
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
import kotlinx.android.synthetic.main.events_fragment.*

class ProfileFragment : Fragment(R.layout.profile_fragment) {
    private val viewModel: ProfileViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        AccountHeaderView(context!!).apply {
            attachToSliderView(ef_slider)
            height = DimenHolder.fromDp(200)
            headerBackground = ImageHolder(R.drawable.landscape)
            addProfile(ProfileDrawerItem().apply {
                name = StringHolder(getActiveUser()!!.name)
                description = StringHolder(getActiveUser()!!.email)
            }, 0)
            selectionListEnabledForSingleProfile = false
        }

        ef_slider.itemAdapter.add(
            PrimaryDrawerItem().apply {
                identifier = 1
                name = StringHolder("Események")
                iconDrawable = ContextCompat.getDrawable(context!!, R.drawable.ic_event)!!
            },
            PrimaryDrawerItem().apply {
                identifier = 2
                name = StringHolder("Profil")
                isSelected = true
                isEnabled = false
                iconDrawable = ContextCompat.getDrawable(context!!, R.drawable.ic_person)!!
            },
            SectionDrawerItem().apply {
                name = StringHolder("Továbbiak")
            },
            SecondaryDrawerItem().apply {
                identifier = 3
                name = StringHolder("Kilépés")
                iconDrawable = ContextCompat.getDrawable(context!!, R.drawable.ic_logout)!!
            }
        )

        ef_slider.onDrawerItemClickListener = { _, item, _ ->
            when (item.identifier) {
                1L -> {
                    findNavController().navigate(R.id.eventsFragment)
                }
                3L -> {
                    setSavedUser(context!!, "null", "null")
                    findNavController().navigate(R.id.registerFragment)
                }
            }
            false
        }
    }
}