package com.gkiss01.meetdeb.screens.fragment

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import com.gkiss01.meetdeb.ActivityViewModel
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.isAdmin
import com.gkiss01.meetdeb.network.Status
import kotlinx.android.synthetic.main.fragment_profile.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private val viewModelActivityKoin: ActivityViewModel by sharedViewModel()

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
        return item.onNavDestinationSelected(findNavController()) || super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModelActivityKoin.activeUser.observe(viewLifecycleOwner, {
            when (it.status) {
                Status.SUCCESS -> {
                    pf_name.text = it.data?.name
                    pf_email.text = it.data?.email

                    pf_id.editText?.setText(String.format("%07d", it.data?.id), TextView.BufferType.NORMAL)

                    if (it.data?.isAdmin() == true) {
                        val color = ContextCompat.getColor(requireContext(), R.color.anzacYellow)

                        pf_rank.text = getString(R.string.profile_admin)
                        pf_rank.setTextColor(color)
                        pf_profileImage.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.anzacYellow)
                    }
                    else pf_rank.text = getString(R.string.profile_user)
                }
                Status.PENDING -> findNavController().setGraph(R.navigation.navigation_graph_start)
                else -> Log.e("MeetDebLog_ProfileFragment", "User is null...")
            }
        })

        viewModelActivityKoin.getEventsSummary().observe(viewLifecycleOwner, {
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
    }

}
