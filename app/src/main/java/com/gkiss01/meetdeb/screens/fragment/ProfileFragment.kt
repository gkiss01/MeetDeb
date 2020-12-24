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
import com.gkiss01.meetdeb.data.remote.response.User
import com.gkiss01.meetdeb.data.remote.response.isAdmin
import com.gkiss01.meetdeb.databinding.FragmentProfileBinding
import com.gkiss01.meetdeb.network.common.Resource.Status
import com.gkiss01.meetdeb.utils.mainActivity
import com.gkiss01.meetdeb.utils.observeEvent
import com.gkiss01.meetdeb.viewmodels.ProfileViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
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
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        viewModelKoin.getEventsSummary()
        viewModelActivityKoin.activeUser.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> it.data?.let { user -> bindUser(user) }
                else -> {}
            }
        }

        // Toast üzenet
        viewModelKoin.toastEvent.observeEvent(viewLifecycleOwner) {
            when (it) {
                is Int -> Toast.makeText(requireContext(), getString(it), Toast.LENGTH_LONG).show()
                is String -> Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }

        // Összefoglaló kezelése
        viewModelKoin.eventsSummary.observe(viewLifecycleOwner) {
            binding.createdEventsLabel.text = it.eventsCreated.toString()
            binding.acceptedEventsLabel.text = it.eventsInvolved.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun bindUser(user: User) {
        binding.nameLabel.text = user.name
        binding.emailLabel.text = user.email
        binding.idField.editText?.setText(String.format("%07d", user.id), TextView.BufferType.NORMAL)

        if (user.isAdmin()) {
            val color = ContextCompat.getColor(requireContext(), R.color.anzacYellow)

            binding.rankLabel.text = getString(R.string.profile_admin)
            binding.rankLabel.setTextColor(color)
            binding.profileImage.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.anzacYellow)
        }
        else binding.rankLabel.text = getString(R.string.profile_user)
    }
}
