package com.gkiss01.meetdeb.adapter

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.gkiss01.meetdeb.data.Event
import org.threeten.bp.format.DateTimeFormatter

private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy, HH:mm")

@BindingAdapter("setDateFormat")
fun TextView.setDateFormat(item: Event) {
    text = item.date.format(formatter)
}

@BindingAdapter("setParticipantsFormat")
fun TextView.setParticipantsFormat(item: Event) {
    val string = "Ott lesz ${item.participants} ember"
    text = string
}