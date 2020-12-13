package com.gkiss01.meetdeb.data.fastadapter

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.view.View
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.adapter.DatePickerViewHolder
import com.gkiss01.meetdeb.utils.isTimeIn24HourFormat
import com.gkiss01.meetdeb.utils.update
import com.mikepenz.fastadapter.items.AbstractItem
import org.threeten.bp.OffsetDateTime

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
