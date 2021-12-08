package com.gkiss01.meetdeb.screens.fragment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.aminography.choosephotohelper.ChoosePhotoHelper
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.remote.response.Event
import com.gkiss01.meetdeb.databinding.FragmentEventCreateBinding
import com.gkiss01.meetdeb.utils.*
import com.gkiss01.meetdeb.viewmodels.EventCreateViewModel
import com.gkiss01.meetdeb.viewmodels.ScreenType
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.threeten.bp.OffsetDateTime
import java.io.File

class EventCreateFragment : Fragment(R.layout.fragment_event_create) {
    private var _binding: FragmentEventCreateBinding? = null
    private val binding get() = _binding!!

    private val viewModelKoin: EventCreateViewModel by viewModel()

    private val safeArgs: EventCreateFragmentArgs by navArgs()

    private lateinit var choosePhotoHelper: ChoosePhotoHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEventCreateBinding.bind(view)

        choosePhotoHelper = ChoosePhotoHelper.with(this)
            .asFilePath()
            .withState(savedInstanceState)
            .build {
                viewModelKoin.pickedImageUri.value = it
            }

        if (!viewModelKoin.isEventInitialized()) {
            safeArgs.event?.let {
                viewModelKoin.eventLocal = it
                viewModelKoin.type = ScreenType.UPDATE
            } ?: run {
                viewModelKoin.eventLocal = Event()
                viewModelKoin.type = ScreenType.NEW
            }
        }

        binding.event = viewModelKoin.eventLocal
        if (viewModelKoin.type == ScreenType.UPDATE) {
            binding.previewImage.load("${Constants.BASE_URL}/images/${viewModelKoin.eventLocal.id}") {
                placeholder(R.drawable.placeholder)
                error(R.drawable.placeholder)
            }
        } else {
            binding.previewImage.load(R.drawable.placeholder)
            viewModelKoin.pickedImageUri.observe(viewLifecycleOwner) {
                if (it != null) binding.previewImage.load(File(it))
                else binding.previewImage.load(R.drawable.placeholder)
            }
        }

        val onDateListener = DatePickerDialog.OnDateSetListener { _, year, monthValue, dayOfMonth ->
            viewModelKoin.eventLocal.date = viewModelKoin.eventLocal.date.update(year, monthValue + 1, dayOfMonth)
            binding.dateLabel.text = viewModelKoin.eventLocal.date.format()
        }

        val onTimeListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            viewModelKoin.eventLocal.date = viewModelKoin.eventLocal.date.update(hourOfDay, minute)
            binding.dateLabel.text = viewModelKoin.eventLocal.date.format()
        }

        binding.dateButton.setOnClickListener {
            val date = viewModelKoin.eventLocal.date
            val datePickerDialog = DatePickerDialog(requireContext(), onDateListener, date.year, date.monthValue - 1, date.dayOfMonth)
            datePickerDialog.show()
        }

        binding.timeButton.setOnClickListener {
            val date = viewModelKoin.eventLocal.date
            val timePickerDialog = TimePickerDialog(requireContext(), onTimeListener, date.hour, date.minute, requireContext().isTimeIn24HourFormat())
            timePickerDialog.show()
        }

        binding.imageButton.setOnClickListener {
            if (viewModelKoin.type == ScreenType.NEW) choosePhotoHelper.showChooser()
            else Toast.makeText(context, getString(R.string.cannot_update_image), Toast.LENGTH_LONG).show()
        }

        binding.createButton.attachTextChangeAnimator()
        binding.createButton.setOnClickListener {
            val isValidName = validateName()
            val isValidDesc = validateDescription()
            val isValidVenue = validateVenue()
            val isValidDate = validateDate()

            if (isValidName && isValidDesc && isValidVenue && isValidDate) {
                hideKeyboard()

                viewModelKoin.uploadEvent()
            }
        }

        // Toast üzenet
        viewModelKoin.toastEvent.observeEvent(viewLifecycleOwner) {
            when (it) {
                is Int -> Toast.makeText(requireContext(), getString(it), Toast.LENGTH_LONG).show()
                is String -> Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }

        // Események létrehozása és frissítése
        viewModelKoin.itemCurrentlyAdding.observe(viewLifecycleOwner) {
            if (it) showAnimation() else hideAnimation()
        }

        viewModelKoin.operationSuccessful.observeEvent(viewLifecycleOwner) {
            binding.createButton.isEnabled = false
            binding.createButton.hideProgress(R.string.done)
            runDelayed { findNavController().navigateUp() }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        choosePhotoHelper.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        choosePhotoHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        choosePhotoHelper.onSaveInstanceState(outState)
    }

    private fun validateName(): Boolean {
        return when {
            viewModelKoin.eventLocal.name.isEmpty() -> {
                binding.nameField.error = getString(R.string.field_required)
                false
            }
            viewModelKoin.eventLocal.name.length > 40 -> {
                binding.nameField.error = getString(R.string.max_event_name_length)
                false
            }
            else -> {
                binding.nameField.error = null
                true
            }
        }
    }

    private fun validateDescription(): Boolean {
        return when {
            viewModelKoin.eventLocal.description.isEmpty() -> {
                binding.descriptionField.error = getString(R.string.field_required)
                false
            }
            else -> {
                binding.descriptionField.error = null
                true
            }
        }
    }

    private fun validateVenue(): Boolean {
        return when {
            viewModelKoin.eventLocal.venue.isEmpty() -> {
                binding.venueField.error = getString(R.string.field_required)
                false
            }
            else -> {
                binding.venueField.error = null
                true
            }
        }
    }

    private fun validateDate(): Boolean {
        return when {
            viewModelKoin.eventLocal.date.isBefore(OffsetDateTime.now()) -> {
                binding.dateLabel.error = getString(R.string.future_date_required)
                false
            }
            else -> {
                binding.dateLabel.error = null
                true
            }
        }
    }

    private fun showAnimation() {
        binding.createButton.showProgress {
            buttonTextRes = if (viewModelKoin.type == ScreenType.NEW) R.string.event_create_waiting else R.string.event_more_update_waiting
            progressColor = Color.WHITE
        }
    }

    private fun hideAnimation() {
        binding.createButton.hideProgress(if (viewModelKoin.type == ScreenType.NEW) R.string.event_create_button else R.string.event_more_update)
    }

    companion object {
        private const val REQUEST_CODE_PICK_IMAGE = 1
    }
}
