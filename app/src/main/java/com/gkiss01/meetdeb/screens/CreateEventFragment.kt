package com.gkiss01.meetdeb.screens

import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.EventDatabase
import com.gkiss01.meetdeb.databinding.CreateEventFragmentBinding
import org.threeten.bp.OffsetDateTime

class CreateEventFragment : Fragment() {

    private lateinit var binding: CreateEventFragmentBinding
    private lateinit var viewModel: CreateEventViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.create_event_fragment, container, false)

        val application = requireNotNull(this.activity).application
        val dataSource = EventDatabase.getInstance(application).eventEntryDao
        val viewModelFactory = CreateEventViewModelFactory(dataSource, application)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(CreateEventViewModel::class.java)

        binding.viewmodel = viewModel

        binding.dateButton.setOnClickListener {
            val datePickerDialog = DatePickerDialog(context!!, OnDateSetListener { _, year, month, dayOfMonth ->

                viewModel.year = year
                viewModel.month = month + 1
                viewModel.day = dayOfMonth
                viewModel.calculateDateTime()

            }, viewModel.year, viewModel.month - 1, viewModel.day)
            datePickerDialog.show()
        }

        val is24HourFormat = android.text.format.DateFormat.is24HourFormat(context)
        binding.timeButton.setOnClickListener {
            val timePickerDialog = TimePickerDialog(context!!, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->

                viewModel.hour = hourOfDay
                viewModel.minute = minute
                viewModel.calculateDateTime()

            }, viewModel.hour, viewModel.minute, is24HourFormat)
            timePickerDialog.show()
        }

        viewModel.dateTime.observe(this, Observer {
            binding.eventDateTime.text = it.format(viewModel.formatter)
        })

        binding.createButton.setOnClickListener {
            var error = false

            if (TextUtils.isEmpty(binding.eventName.text)) {
                binding.eventName.error = "A mezőt kötelező kitölteni!"
                error = true
            }

            if (TextUtils.isEmpty(binding.eventLabels.text)) {
                binding.eventLabels.error = "A mezőt kötelező kitölteni!"
                error = true
            }
            if (TextUtils.isEmpty(binding.eventVenue.text)) {
                binding.eventVenue.error = "A mezőt kötelező kitölteni!"
                error = true
            }
            if (viewModel.dateTime.value!!.isBefore(OffsetDateTime.now())) {
                binding.eventDateTime.error = "Jövőbeli dátumot adj meg!"
                error = true
            }
            else binding.eventDateTime.error = null

            if (!error) {
                viewModel.createEvent()

                val inputMethodManager = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(view!!.windowToken, 0)

                val action = CreateEventFragmentDirections.actionCreateEventFragmentToEventsFragment()
                NavHostFragment.findNavController(this).navigate(action)
            }
        }

        return binding.root
    }
}
