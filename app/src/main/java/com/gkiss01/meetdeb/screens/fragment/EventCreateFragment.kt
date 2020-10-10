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
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.fastadapter.Event
import com.gkiss01.meetdeb.data.fastadapter.format
import com.gkiss01.meetdeb.data.fastadapter.isTimeIn24HourFormat
import com.gkiss01.meetdeb.data.fastadapter.update
import com.gkiss01.meetdeb.databinding.FragmentEventCreateBinding
import com.gkiss01.meetdeb.network.BASE_URL
import com.gkiss01.meetdeb.utils.observeEvent
import com.gkiss01.meetdeb.viewmodels.EventCreateViewModel
import com.gkiss01.meetdeb.viewmodels.ScreenType
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.squareup.picasso.Picasso
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.GlideEngine
import kotlinx.android.synthetic.main.fragment_event_create.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.threeten.bp.OffsetDateTime

class EventCreateFragment : Fragment() {
    private lateinit var binding: FragmentEventCreateBinding
    private val viewModelKoin: EventCreateViewModel by viewModel()
    private val safeArgs: EventCreateFragmentArgs by navArgs()

    private val REQUEST_CODE_PICK_IMAGE = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_event_create, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
        Picasso.get()
            .load("$BASE_URL/images/${viewModelKoin.eventLocal.id}")
            .placeholder(R.drawable.placeholder)
            .into(cef_imagePreview)

        val onDateListener = DatePickerDialog.OnDateSetListener { _, year, monthValue, dayOfMonth ->
            viewModelKoin.eventLocal.date = viewModelKoin.eventLocal.date.update(year, monthValue + 1, dayOfMonth)
            cef_dateTitle.text = viewModelKoin.eventLocal.date.format()
        }

        val onTimeListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            viewModelKoin.eventLocal.date = viewModelKoin.eventLocal.date.update(hourOfDay, minute)
            cef_dateTitle.text = viewModelKoin.eventLocal.date.format()
        }

        cef_dateButton.setOnClickListener {
            val datePickerDialog = DatePickerDialog(requireContext(), onDateListener, viewModelKoin.eventLocal.date.year, viewModelKoin.eventLocal.date.monthValue - 1, viewModelKoin.eventLocal.date.dayOfMonth)
            datePickerDialog.show()
        }

        cef_timeButton.setOnClickListener {
            val timePickerDialog = TimePickerDialog(context, onTimeListener, viewModelKoin.eventLocal.date.hour, viewModelKoin.eventLocal.date.minute, requireContext().isTimeIn24HourFormat())
            timePickerDialog.show()
        }

        cef_imageButton.setOnClickListener {
            if (viewModelKoin.type == ScreenType.ADD) requestStoragePermissions()
            else Toast.makeText(context, getString(R.string.cannot_update_image), Toast.LENGTH_LONG).show()
        }

        cef_createButton.attachTextChangeAnimator()
        cef_createButton.setOnClickListener {
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
            cef_createButton.isEnabled = false
            cef_createButton.hideProgress(R.string.done)
            Handler().postDelayed({ findNavController().popBackStack(R.id.eventsFragment, false) }, 500)
        }

        viewModelKoin.pickedImageUri.observe(viewLifecycleOwner) {
            val bmImg: Bitmap = BitmapFactory.decodeFile(it)
            cef_imagePreview.setImageBitmap(bmImg)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK) {
            viewModelKoin.pickedImageUri.value = Matisse.obtainPathResult(data)[0]
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
        Matisse.from(this)
            .choose(MimeType.ofImage())
            .theme(R.style.Matisse_Dracula)
            .countable(true)
            .maxSelectable(1)
            .gridExpectedSize(resources.getDimensionPixelSize(R.dimen.grid_expected_size))
            .imageEngine(GlideEngine())
            .autoHideToolbarOnSingleTap(true)
            .forResult(REQUEST_CODE_PICK_IMAGE)
    }

    private fun validateName(): Boolean {
        return when {
            viewModelKoin.eventLocal.name.isEmpty() -> {
                cef_name.error = getString(R.string.field_required)
                false
            }
            viewModelKoin.eventLocal.name.length > 40 -> {
                cef_name.error = getString(R.string.max_event_name_length)
                false
            }
            else -> {
                cef_name.error = null
                true
            }
        }
    }

    private fun validateDescription(): Boolean {
        return when {
            viewModelKoin.eventLocal.description.isEmpty() -> {
                cef_description.error = getString(R.string.field_required)
                false
            }
            else -> {
                cef_description.error = null
                true
            }
        }
    }

    private fun validateVenue(): Boolean {
        return when {
            viewModelKoin.eventLocal.venue.isEmpty() -> {
                cef_venue.error = getString(R.string.field_required)
                false
            }
            else -> {
                cef_venue.error = null
                true
            }
        }
    }

    private fun validateDate(): Boolean {
        return when {
            viewModelKoin.eventLocal.date.isBefore(OffsetDateTime.now()) -> {
                cef_dateTitle.error = getString(R.string.future_date_required)
                false
            }
            else -> {
                cef_dateTitle.error = null
                true
            }
        }
    }

    private fun showAnimation() {
        cef_createButton.showProgress {
            buttonTextRes = if (viewModelKoin.type == ScreenType.ADD) R.string.event_create_waiting else R.string.event_more_update_waiting
            progressColor = Color.WHITE
        }
    }

    private fun hideAnimation() {
        cef_createButton.hideProgress(if (viewModelKoin.type == ScreenType.ADD) R.string.event_create_button else R.string.event_more_update)
    }
}
