package com.gkiss01.meetdeb.data.fastadapter

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.text.format.DateFormat
import android.view.View
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.adapter.DatePickerViewHolder
import com.mikepenz.fastadapter.items.AbstractItem
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter

data class DatePickerItem(var offsetDateTime: OffsetDateTime): AbstractItem<DatePickerViewHolder>() {
    constructor(): this(OffsetDateTime.now())

    override val layoutRes: Int
        get() = R.layout.item_date_picker
    override val type: Int
        get() = R.id.dlp_layout

    override fun bindView(holder: DatePickerViewHolder, payloads: List<Any>) {
        super.bindView(holder, payloads)
        val context = holder.itemView.context

        holder.binding.headerLayout.setOnClickListener { holder.closeOrExpand() }

        holder.binding.dateButton.setOnClickListener {
            val datePickerDialog = DatePickerDialog(context, { _, year, monthValue, dayOfMonth ->
                offsetDateTime = offsetDateTime.update(year, monthValue + 1, dayOfMonth)
                holder.updateSelectedDate(offsetDateTime)
            }, offsetDateTime.year, offsetDateTime.monthValue - 1, offsetDateTime.dayOfMonth)
            datePickerDialog.show()
        }

        holder.binding.timeButton.setOnClickListener {
            val timePickerDialog = TimePickerDialog(context, { _, hourOfDay, minute ->
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