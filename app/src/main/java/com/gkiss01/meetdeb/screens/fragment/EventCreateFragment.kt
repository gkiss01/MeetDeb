package com.gkiss01.meetdeb.screens.fragment

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.remote.response.Event
import com.gkiss01.meetdeb.databinding.FragmentEventCreateBinding
import com.gkiss01.meetdeb.network.common.BASE_URL
import com.gkiss01.meetdeb.utils.*
import com.gkiss01.meetdeb.viewmodels.EventCreateViewModel
import com.gkiss01.meetdeb.viewmodels.ScreenType
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.opensooq.supernova.gligar.GligarPicker
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.threeten.bp.OffsetDateTime

class EventCreateFragment : Fragment(R.layout.fragment_event_create) {
    private var _binding: FragmentEventCreateBinding? = null
    private val binding get() = _binding!!

    private val viewModelKoin: EventCreateViewModel by viewModel()

    private val safeArgs: EventCreateFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEventCreateBinding.bind(view)

        if (!viewModelKoin.isEventInitialized()) {
            safeArgs.event?.let {
                viewModelKoin.eventLocal = it
                viewModelKoin.type = ScreenType.UPDATE
            } ?: run {
                viewModelKoin.eventLocal = Event()
                viewModelKoin.type = ScreenType.ADD
            }
        }

        binding.event = viewModelKoin.eventLocal
        binding.previewImage.load("$BASE_URL/images/${viewModelKoin.eventLocal.id}") {
            placeholder(R.drawable.placeholder)
            error(R.drawable.placeholder)
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
            val datePickerDialog = DatePickerDialog(requireContext(), onDateListener, viewModelKoin.eventLocal.date.year, viewModelKoin.eventLocal.date.monthValue - 1, viewModelKoin.eventLocal.date.dayOfMonth)
            datePickerDialog.show()
        }

        binding.timeButton.setOnClickListener {
            val timePickerDialog = TimePickerDialog(context, onTimeListener, viewModelKoin.eventLocal.date.hour, viewModelKoin.eventLocal.date.minute, requireContext().isTimeIn24HourFormat())
            timePickerDialog.show()
        }

        binding.imageButton.setOnClickListener {
            if (viewModelKoin.type == ScreenType.ADD) requestStoragePermissions()
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

        viewModelKoin.pickedImageUri.observe(viewLifecycleOwner) {
            val bmImg: Bitmap = BitmapFactory.decodeFile(it)
            binding.previewImage.setImageBitmap(bmImg)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK) {
            val imagesList = data?.extras?.getStringArray(GligarPicker.IMAGES_RESULT)
            if (!imagesList.isNullOrEmpty()) {
                viewModelKoin.pickedImageUri.value = imagesList.first()
            }
        }
    }

    private fun requestStoragePermissions() {
        Dexter.withContext(activity)
            .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object: MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let {
                        if(it.areAllPermissionsGranted()) showImagePicker()
                    }
                }
                override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                    token?.continuePermissionRequest()
                }
            })
            .check()
    }

    private fun showImagePicker() {
        GligarPicker().requestCode(REQUEST_CODE_PICK_IMAGE).withFragment(this).limit(1).show()
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
            buttonTextRes = if (viewModelKoin.type == ScreenType.ADD) R.string.event_create_waiting else R.string.event_more_update_waiting
            progressColor = Color.WHITE
        }
    }

    private fun hideAnimation() {
        binding.createButton.hideProgress(if (viewModelKoin.type == ScreenType.ADD) R.string.event_create_button else R.string.event_more_update)
    }

    companion object {
        private const val REQUEST_CODE_PICK_IMAGE = 1
    }
}
