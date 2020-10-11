package com.gkiss01.meetdeb.screens.fragment

import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import com.gkiss01.meetdeb.ActivityViewModel
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.User
import com.gkiss01.meetdeb.data.isAdmin
import com.gkiss01.meetdeb.network.Status
import com.gkiss01.meetdeb.utils.mainActivity
import com.gkiss01.meetdeb.utils.observeEvent
import com.gkiss01.meetdeb.viewmodels.ProfileViewModel
import kotlinx.android.synthetic.main.fragment_profile.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private val viewModelActivityKoin: ActivityViewModel by sharedViewModel()
    private val viewModelKoin: ProfileViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.action_profile_fragment, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(findNavController()) || handleCustomActions(item) || super.onOptionsItemSelected(item)
    }

    private fun handleCustomActions(item: MenuItem) = when(item.itemId) {
        R.id.action_logout -> {
            viewModelActivityKoin.logout()
            mainActivity?.changeNavGraphToStart()
            true
        }
        else -> false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModelKoin.getEventsSummary()
        viewModelActivityKoin.activeUser.observe(viewLifecycleOwner, {
            when (it.status) {
                Status.SUCCESS -> it.data?.let { user -> bindUser(user) }
                else -> {}
            }
        })

        // Toast üzenet
        viewModelKoin.toastEvent.observeEvent(viewLifecycleOwner) {
            when (it) {
                is Int -> Toast.makeText(requireContext(), getString(it), Toast.LENGTH_LONG).show()
                is String -> Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }

        // Összefoglaló kezelése
        viewModelKoin.eventsSummary.observe(viewLifecycleOwner) {
            pf_createdEvents.text = it.eventsCreated.toString()
            pf_acceptedEvents.text = it.eventsInvolved.toString()
        }
    }

    private fun bindUser(user: User) {
        pf_name.text = user.name
        pf_email.text = user.email
        pf_id.editText?.setText(String.format("%07d", user.id), TextView.BufferType.NORMAL)

        if (user.isAdmin()) {
            val color = ContextCompat.getColor(requireContext(), R.color.anzacYellow)

            pf_rank.text = getString(R.string.profile_admin)
            pf_rank.setTextColor(color)
            pf_profileImage.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.anzacYellow)
        }
        else pf_rank.text = getString(R.string.profile_user)
    }
}
