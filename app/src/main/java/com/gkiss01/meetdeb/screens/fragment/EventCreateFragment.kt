package com.gkiss01.meetdeb.screens.fragment

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.fastadapter.Event
import com.gkiss01.meetdeb.databinding.FragmentEventCreateBinding
import com.gkiss01.meetdeb.network.BASE_URL
import com.gkiss01.meetdeb.network.ErrorCodes
import com.gkiss01.meetdeb.network.NavigationCode
import com.gkiss01.meetdeb.utils.formatDate
import com.gkiss01.meetdeb.utils.hideKeyboard
import com.gkiss01.meetdeb.utils.isDate24HourFormat
import com.gkiss01.meetdeb.utils.updateOffsetDateTime
import com.gkiss01.meetdeb.viewmodels.CreateEventViewModel
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
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.threeten.bp.OffsetDateTime

class EventCreateFragment : Fragment() {
    private lateinit var binding: FragmentEventCreateBinding
    private val viewModel: CreateEventViewModel by viewModels()

    private val REQUEST_CODE_PICK_IMAGE = 1

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onErrorReceived(errorCode: ErrorCodes) {
        if (errorCode == ErrorCodes.UNKNOWN) {
            cef_createButton.hideProgress(if (viewModel.type.value == ScreenType.NEW) R.string.event_create_button else R.string.event_more_update)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNavigationReceived(navigationCode: NavigationCode) {
        if (navigationCode == NavigationCode.NAVIGATE_TO_EVENTS_FRAGMENT) {
            cef_createButton.hideProgress(R.string.done)
            Handler().postDelayed({ findNavController().popBackStack(R.id.eventsFragment, false) }, 500)
        }
        else if (navigationCode == NavigationCode.NAVIGATE_TO_IMAGE_PICKER) showImagePicker()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_event_create, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val argEvent = arguments?.getSerializable("event") as Event?
        if (viewModel.type.value == ScreenType.NONE) {
            if (argEvent != null) {
                viewModel.type.value = ScreenType.UPDATE
                viewModel.event = argEvent
            } else viewModel.type.value = ScreenType.NEW
        }

        binding.event = viewModel.event
        Picasso.get()
            .load("$BASE_URL/images/${viewModel.event.id}")
            .placeholder(R.drawable.placeholder)
            .into(cef_imagePreview)

        cef_dateButton.setOnClickListener {
            val datePickerDialog = DatePickerDialog(context!!, DatePickerDialog.OnDateSetListener { _, year, monthValue, dayOfMonth ->
                viewModel.event.date = updateOffsetDateTime(viewModel.event.date, year, monthValue + 1, dayOfMonth)
                cef_dateTitle.text = formatDate(viewModel.event.date)
            }, viewModel.event.date.year, viewModel.event.date.monthValue - 1, viewModel.event.date.dayOfMonth)
            datePickerDialog.show()
        }

        cef_timeButton.setOnClickListener {
            val timePickerDialog = TimePickerDialog(context, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                viewModel.event.date = updateOffsetDateTime(viewModel.event.date, hourOfDay, minute)
                cef_dateTitle.text = formatDate(viewModel.event.date)
            }, viewModel.event.date.hour, viewModel.event.date.minute, isDate24HourFormat(context!!))
            timePickerDialog.show()
        }

        cef_imageButton.setOnClickListener {
            if (viewModel.type.value == ScreenType.NEW) requestStoragePermissions()
            else Toast.makeText(context, "A képet nem tudod frissíteni!", Toast.LENGTH_LONG).show()
        }

        viewModel.type.observe(viewLifecycleOwner, Observer {
            cef_createButton.text = getString(if (it == ScreenType.NEW) R.string.event_create_button else R.string.event_more_update)
        })

        cef_createButton.attachTextChangeAnimator()
        cef_createButton.setOnClickListener {
            var error = false

            if (TextUtils.isEmpty(viewModel.event.name)) {
                cef_name.error = "A mezőt kötelező kitölteni!"
                error = true
            }
            else if (viewModel.event.name.length > 40) {
                cef_name.error = "A név max. 40 karakter lehet!"
                error = true
            }

            if (TextUtils.isEmpty(viewModel.event.description)) {
                cef_description.error = "A mezőt kötelező kitölteni!"
                error = true
            }

            if (TextUtils.isEmpty(viewModel.event.venue)) {
                cef_venue.error = "A mezőt kötelező kitölteni!"
                error = true
            }

            if (viewModel.event.date.isBefore(OffsetDateTime.now())) {
                cef_dateTitle.error = "Jövőbeli dátumot adj meg!"
                error = true
            }
            else cef_dateTitle.error = null

            if (!error) {
                hideKeyboard(context!!, view)

                cef_createButton.showProgress {
                    buttonTextRes = if (viewModel.type.value == ScreenType.NEW) R.string.event_create_waiting else R.string.event_more_update_waiting
                    progressColor = Color.WHITE
                }
                viewModel.uploadEvent()
            }
        }
    }

    private fun requestStoragePermissions() {
        Dexter.withActivity(activity)
            .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object: MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let {
                        if(report.areAllPermissionsGranted()) showImagePicker()
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
            .theme(R.style.styleMatisseDark)
            .countable(true)
            .maxSelectable(1)
            .gridExpectedSize(resources.getDimensionPixelSize(R.dimen.grid_expected_size))
            .imageEngine(GlideEngine())
            .autoHideToolbarOnSingleTap(true)
            .forResult(REQUEST_CODE_PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK) {
            viewModel.imageUrl = Matisse.obtainPathResult(data)[0]
            cef_imagePreview.setImageURI(Matisse.obtainResult(data)[0])
        }
    }
}
