package com.gkiss01.meetdeb.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment

import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.databinding.MenuFragmentBinding

class MenuFragment : Fragment() {

    private lateinit var binding: MenuFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.menu_fragment, container, false)

        binding.eventsButton.setOnClickListener { run {
            val action = MenuFragmentDirections.actionMenuFragmentToEventsFragment()
            NavHostFragment.findNavController(this).navigate(action)
        }}
        binding.webButton.setOnClickListener { run {
            val action = MenuFragmentDirections.actionMenuFragmentToWebContentFragment()
            NavHostFragment.findNavController(this).navigate(action)
        }}

        return binding.root
    }
}
