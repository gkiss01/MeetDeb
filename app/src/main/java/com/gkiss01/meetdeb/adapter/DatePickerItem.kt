package com.gkiss01.meetdeb.adapter

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.view.View
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.utils.updateOffsetDateTime
import com.mikepenz.fastadapter.items.AbstractItem
import kotlinx.android.synthetic.main.dates_list_addition.view.*
import org.threeten.bp.OffsetDateTime

class DatePickerItem: AbstractItem<DatePickerViewHolder>() {
    var offsetDateTime: OffsetDateTime = OffsetDateTime.now()

    override val type: Int
        get() = R.id.dla_layout
    override val layoutRes: Int
        get() = R.layout.dates_list_addition

    override fun bindView(holder: DatePickerViewHolder, payloads: List<Any>) {
        super.bindView(holder, payloads)
        val context = holder.itemView.context
        val is24HourFormat = android.text.format.DateFormat.is24HourFormat(context)

        holder.itemView.dla_headerLayout.setOnClickListener { holder.closeOrExpand() }

        holder.itemView.dla_dateButton.setOnClickListener {
            val datePickerDialog = DatePickerDialog(context, DatePickerDialog.OnDateSetListener { _, year, monthValue, dayOfMonth ->
                offsetDateTime = updateOffsetDateTime(offsetDateTime, year, monthValue + 1, dayOfMonth)
                holder.updateSelectedDate(offsetDateTime)
            }, offsetDateTime.year, offsetDateTime.monthValue - 1, offsetDateTime.dayOfMonth)
            datePickerDialog.show()
        }

        holder.itemView.dla_timeButton.setOnClickListener {
            val timePickerDialog = TimePickerDialog(context, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                offsetDateTime = updateOffsetDateTime(offsetDateTime, hourOfDay, minute)
                holder.updateSelectedDate(offsetDateTime)
            }, offsetDateTime.hour, offsetDateTime.minute, is24HourFormat)
            timePickerDialog.show()
        }
    }

    override fun getViewHolder(v: View): DatePickerViewHolder = DatePickerViewHolder(v)
}