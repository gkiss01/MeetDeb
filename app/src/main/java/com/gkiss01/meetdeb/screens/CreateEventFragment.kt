package com.gkiss01.meetdeb.screens

import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.databinding.CreateEventFragmentBinding
import com.gkiss01.meetdeb.network.BASE_URL
import com.gkiss01.meetdeb.network.GlideApp
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.GlideEngine
import org.threeten.bp.OffsetDateTime

class CreateEventFragment : Fragment() {

    private val REQUEST_CODE_PICK_IMAGE = 1
    private val PERMISSION_CODE_STORAGE = 2

    private lateinit var binding: CreateEventFragmentBinding
    private lateinit var viewModel: CreateEventViewModel

    private lateinit var filePath: Uri

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.create_event_fragment, container, false)

        val application = requireNotNull(this.activity).application
        val viewModelFactory = CreateEventViewModelFactory(application)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(CreateEventViewModel::class.java)

        binding.viewmodel = viewModel

        binding.dateButton.setOnClickListener {
            val datePickerDialog = DatePickerDialog(context!!, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->

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

        binding.imageButton.setOnClickListener {
            requestStoragePermissions()
        }

        GlideApp.with(this)
            .load("$BASE_URL/images/10")
            .placeholder(R.drawable.fab_label_background)
            .error(R.drawable.fab_label_background)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.imagePreview)

        return binding.root
    }

    private fun requestStoragePermissions() {
        val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if(hasPermissions(context!!, permissions.toList())) {
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
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(activity, "Permission granted!", Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(activity, "Permission denied!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK) {
            filePath = Matisse.obtainResult(data)[0]

            binding.imagePreview.setImageURI(filePath)
        }
    }
}