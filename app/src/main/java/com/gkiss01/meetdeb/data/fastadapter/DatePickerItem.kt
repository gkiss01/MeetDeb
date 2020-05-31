package com.gkiss01.meetdeb.data.fastadapter

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.text.format.DateFormat
import android.view.View
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.adapter.DatePickerViewHolder
import com.mikepenz.fastadapter.items.AbstractItem
import kotlinx.android.synthetic.main.item_date_picker.view.*
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter

class DatePickerItem: AbstractItem<DatePickerViewHolder>() {
    var offsetDateTime: OffsetDateTime = OffsetDateTime.now()

    override val layoutRes: Int
        get() = R.layout.item_date_picker
    override val type: Int
        get() = R.id.dlp_layout

    override fun bindView(holder: DatePickerViewHolder, payloads: List<Any>) {
        super.bindView(holder, payloads)
        val context = holder.itemView.context

        holder.itemView.dlp_headerLayout.setOnClickListener { holder.closeOrExpand() }

        holder.itemView.dlp_dateButton.setOnClickListener {
            val datePickerDialog = DatePickerDialog(context, DatePickerDialog.OnDateSetListener { _, year, monthValue, dayOfMonth ->
                offsetDateTime = offsetDateTime.update(year, monthValue + 1, dayOfMonth)
                holder.updateSelectedDate(offsetDateTime)
            }, offsetDateTime.year, offsetDateTime.monthValue - 1, offsetDateTime.dayOfMonth)
            datePickerDialog.show()
        }

        holder.itemView.dlp_timeButton.setOnClickListener {
            val timePickerDialog = TimePickerDialog(context, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                offsetDateTime = offsetDateTime.update(hourOfDay, minute)
                holder.updateSelectedDate(offsetDateTime)
            }, offsetDateTime.hour, offsetDateTime.minute, context.isTimeIn24HourFormat())
            timePickerDialog.show()
        }
    }

    override fun getViewHolder(v: View): DatePickerViewHolder =
        DatePickerViewHolder(v)
}

fun OffsetDateTime.update(year: Int, monthValue: Int, dayOfMonth: Int): OffsetDateTime = OffsetDateTime.of(year, monthValue, dayOfMonth, this.hour, this.minute,
    this.second, this.nano, this.offset)

fun OffsetDateTime.update(hourOfDay: Int, minute: Int): OffsetDateTime = OffsetDateTime.of(this.year, this.monthValue, this.dayOfMonth, hourOfDay, minute,
    this.second, this.nano, this.offset)

fun OffsetDateTime.format(): String = this.format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy, HH:mm"))

fun Context.isTimeIn24HourFormat() = DateFormat.is24HourFormat(this)