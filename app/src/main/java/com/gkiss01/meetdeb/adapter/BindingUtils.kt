package com.gkiss01.meetdeb.adapter

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.gkiss01.meetdeb.data.EventEntry
import com.gkiss01.meetdeb.network.data.AlbumProperty
import org.threeten.bp.format.DateTimeFormatter

private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy, HH:mm")

@BindingAdapter("setDateFormat")
fun TextView.setDateFormat(item: EventEntry) {
    text = item.date!!.format(formatter)
}

@BindingAdapter("setParticipantsFormat")
fun TextView.setParticipantsFormat(item: EventEntry) {
    val string = "Ott lesz ${item.participants!!.size} ember"
    text = string
}

@BindingAdapter("setUserId")
fun TextView.setUserId(item: AlbumProperty) {
    val string = "Felhasználó azonosító: ${item.userId.toInt()}"
    text = string
}

@BindingAdapter("setAlbumId")
fun TextView.setAlbumId(item: AlbumProperty) {
    val string = "Album azonosító: ${item.id.toInt()}"
    text = string
}
