package com.gkiss01.meetdeb.data.fastadapter

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.view.View
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.adapter.DatePickerViewHolder
import com.gkiss01.meetdeb.utils.isDate24HourFormat
import com.gkiss01.meetdeb.utils.updateOffsetDateTime
import com.mikepenz.fastadapter.items.AbstractItem
import kotlinx.android.synthetic.main.dates_list_picker.view.*
import org.threeten.bp.OffsetDateTime

class DatePickerItem: AbstractItem<DatePickerViewHolder>() {
    var offsetDateTime: OffsetDateTime = OffsetDateTime.now()

    override val layoutRes: Int
        get() = R.layout.dates_list_picker
    override val type: Int
        get() = R.id.dlp_layout

    override fun bindView(holder: DatePickerViewHolder, payloads: List<Any>) {
        super.bindView(holder, payloads)
        val context = holder.itemView.context

        holder.itemView.dlp_headerLayout.setOnClickListener { holder.closeOrExpand() }

        holder.itemView.dlp_dateButton.setOnClickListener {
            val datePickerDialog = DatePickerDialog(context, DatePickerDialog.OnDateSetListener { _, year, monthValue, dayOfMonth ->
                offsetDateTime = updateOffsetDateTime(offsetDateTime, year, monthValue + 1, dayOfMonth)
                holder.updateSelectedDate(offsetDateTime)
            }, offsetDateTime.year, offsetDateTime.monthValue - 1, offsetDateTime.dayOfMonth)
            datePickerDialog.show()
        }

        holder.itemView.dlp_timeButton.setOnClickListener {
            val timePickerDialog = TimePickerDialog(context, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                offsetDateTime = updateOffsetDateTime(offsetDateTime, hourOfDay, minute)
                holder.updateSelectedDate(offsetDateTime)
            }, offsetDateTime.hour, offsetDateTime.minute, isDate24HourFormat(context))
            timePickerDialog.show()
        }
    }

    override fun getViewHolder(v: View): DatePickerViewHolder =
        DatePickerViewHolder(v)
}