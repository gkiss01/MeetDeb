package com.gkiss01.meetdeb.data.local

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.databinding.ItemDatePickerBinding
import com.gkiss01.meetdeb.utils.format
import com.gkiss01.meetdeb.utils.isTimeIn24HourFormat
import com.gkiss01.meetdeb.utils.update
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import org.threeten.bp.OffsetDateTime

class DatePickingItem(private val onClickedCreate: ((OffsetDateTime) -> Unit)? = null): AbstractBindingItem<ItemDatePickerBinding>() {
    private var offsetDateTime: OffsetDateTime = OffsetDateTime.now()

    private lateinit var binding: ItemDatePickerBinding
    private var isExpanded: Boolean = false

    override val type: Int
        get() = R.id.dlp_layout
    override var identifier: Long
        get() = 123451
        set(_) {}

    override fun bindView(binding: ItemDatePickerBinding, payloads: List<Any>) {
        this.binding = binding
        val context = binding.root.context

        binding.headerLayout.setOnClickListener { closeOrExpand() }

        binding.dateButton.setOnClickListener {
            val updateBlock = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                offsetDateTime = offsetDateTime.update(year, month + 1, dayOfMonth)
                binding.dateLabel.text = offsetDateTime.format()
            }
            val datePickerDialog = DatePickerDialog(context, updateBlock, offsetDateTime.year, offsetDateTime.monthValue - 1, offsetDateTime.dayOfMonth)
            datePickerDialog.show()
        }

        binding.timeButton.setOnClickListener {
            val updateBlock = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                offsetDateTime = offsetDateTime.update(hourOfDay, minute)
                binding.dateLabel.text = offsetDateTime.format()
            }
            val timePickerDialog = TimePickerDialog(context, updateBlock, offsetDateTime.hour, offsetDateTime.minute, context.isTimeIn24HourFormat())
            timePickerDialog.show()
        }

        binding.createButton.setOnClickListener {
            if (offsetDateTime.isBefore(OffsetDateTime.now())) {
                setError(context.getString(R.string.future_date_required))
            } else {
                setError(null)
                onClickedCreate?.let { it(offsetDateTime) }
            }
        }

        payloads.filterIsInstance<OffsetDateTime>().firstOrNull()?.let {
            offsetDateTime = it
            isExpanded = true

            manageAnimation(true, it)
        } ?: manageAnimation(false, offsetDateTime)

        payloads.filterIsInstance<String>().firstOrNull()?.let {
            if (it == REQUEST_CLOSE_PICKER) isExpanded = false
        }

        binding.subLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
        binding.downArrow.rotation = if (isExpanded) 180F else 0F
    }

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ItemDatePickerBinding {
        return ItemDatePickerBinding.inflate(inflater, parent, false)
    }

    private fun closeOrExpand() {
        isExpanded = !isExpanded
        binding.subLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
        binding.downArrow.animate().setDuration(200).rotation(if (isExpanded) 180F else 0F)
    }

    private fun setError(error: String?) {
        binding.dateLabel.error = error
    }

    private fun manageAnimation(show: Boolean, item: OffsetDateTime) {
        if (show) showAnimation(item) else clearAnimations(item)
    }

    private fun clearAnimations(item: OffsetDateTime) {
        binding.dateLabel.text = item.format()
        binding.createButton.hideProgress(R.string.date_create_button)

        binding.dateButton.isEnabled = true
        binding.timeButton.isEnabled = true
        binding.createButton.isEnabled = true
    }

    private fun showAnimation(item: OffsetDateTime) {
        binding.dateLabel.text = item.format()
        binding.createButton.showProgress {
            buttonTextRes = R.string.date_create_waiting
            progressColor = Color.WHITE
        }

        binding.dateButton.isEnabled = false
        binding.timeButton.isEnabled = false
        binding.createButton.isEnabled = false
    }

    companion object {
        const val REQUEST_CLOSE_PICKER = "REQUEST_CLOSE_PICKER"
    }
}