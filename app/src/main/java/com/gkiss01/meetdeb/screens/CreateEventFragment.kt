package com.gkiss01.meetdeb.screens

import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.fastadapter.Event
import com.gkiss01.meetdeb.network.ErrorCodes
import com.gkiss01.meetdeb.network.NavigationCode
import com.gkiss01.meetdeb.utils.dateFormatter
import com.gkiss01.meetdeb.utils.hideKeyboard
import com.gkiss01.meetdeb.utils.isDate24HourFormat
import com.gkiss01.meetdeb.utils.updateOffsetDateTime
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.GlideEngine
import kotlinx.android.synthetic.main.create_event_fragment.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.threeten.bp.OffsetDateTime

class CreateEventFragment : Fragment(R.layout.create_event_fragment) {
    private val viewModel: CreateEventViewModel by viewModels()

    private val REQUEST_CODE_PICK_IMAGE = 1
    private val PERMISSION_CODE_STORAGE = 2

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
            cef_createButton.hideProgress(R.string.event_create_button)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNavigationReceived(navigationCode: NavigationCode) {
        if (navigationCode == NavigationCode.NAVIGATE_TO_EVENTS_FRAGMENT) {
            cef_createButton.hideProgress(R.string.event_created)
            Handler().postDelayed({ findNavController().popBackStack() }, 500)
        }
        else if (navigationCode == NavigationCode.NAVIGATE_TO_IMAGE_PICKER) showImagePicker()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.event = (arguments?.getSerializable("event") as Event?)?:
                Event(Long.MIN_VALUE, "", Long.MIN_VALUE, "", OffsetDateTime.now(), "", "", false, 0,
                    accepted = false,
                    voted = false
                )

        cef_dateButton.setOnClickListener {
            val datePickerDialog = DatePickerDialog(context!!, DatePickerDialog.OnDateSetListener { _, year, monthValue, dayOfMonth ->
                viewModel.eventDate = updateOffsetDateTime(viewModel.eventDate, year, monthValue + 1, dayOfMonth)
                cef_dateTitle.text = viewModel.eventDate.format(dateFormatter)
            }, viewModel.eventDate.year, viewModel.eventDate.monthValue - 1, viewModel.eventDate.dayOfMonth)
            datePickerDialog.show()
        }

        cef_timeButton.setOnClickListener {
            val timePickerDialog = TimePickerDialog(context, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                viewModel.eventDate = updateOffsetDateTime(viewModel.eventDate, hourOfDay, minute)
                cef_dateTitle.text = viewModel.eventDate.format(dateFormatter)
            }, viewModel.eventDate.hour, viewModel.eventDate.minute, isDate24HourFormat(context!!))
            timePickerDialog.show()
        }

        cef_imageButton.setOnClickListener {
            requestStoragePermissions()
        }

        cef_createButton.attachTextChangeAnimator()
        cef_createButton.setOnClickListener {
            var error = false
            viewModel.eventName = cef_name.text.toString()
            viewModel.eventDescription = cef_description.text.toString()
            viewModel.eventVenue = cef_venue.text.toString()

            if (TextUtils.isEmpty(cef_name.text)) {
                cef_name.error = "A mezőt kötelező kitölteni!"
                error = true
            }
            else if (cef_name.text.length > 40) {
                cef_name.error = "A név max. 40 karakter lehet!"
                error = true
            }

            if (TextUtils.isEmpty(cef_description.text)) {
                cef_description.error = "A mezőt kötelező kitölteni!"
                error = true
            }

            if (TextUtils.isEmpty(cef_venue.text)) {
                cef_venue.error = "A mezőt kötelező kitölteni!"
                error = true
            }

            if (viewModel.eventDate.isBefore(OffsetDateTime.now())) {
                cef_dateTitle.error = "Jövőbeli dátumot adj meg!"
                error = true
            }
            else cef_dateTitle.error = null

            if (!error) {
                hideKeyboard(context!!, view)

                cef_createButton.showProgress {
                    buttonTextRes = R.string.event_create_waiting
                    progressColor = Color.WHITE
                }
                viewModel.createEvent()
            }
        }
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

    private fun requestStoragePermissions() {
        val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if(hasPermissions(context!!, permissions.toList())) showImagePicker()
        else ActivityCompat.requestPermissions(activity!!, permissions, PERMISSION_CODE_STORAGE)
    }

    private fun hasPermissions(context: Context, permissions: List<String>): Boolean {
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_CODE_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                EventBus.getDefault().post(NavigationCode.NAVIGATE_TO_IMAGE_PICKER)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK) {
            cef_imagePreview.setImageURI(Matisse.obtainResult(data)[0])

            viewModel.imageUrl.value = Matisse.obtainPathResult(data)[0]
        }
    }
}
