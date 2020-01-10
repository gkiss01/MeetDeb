package com.gkiss01.meetdeb.screens

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.gkiss01.meetdeb.data.Event
import com.gkiss01.meetdeb.data.GenericResponse
import com.gkiss01.meetdeb.network.WebApi
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import okhttp3.Credentials
import java.net.ConnectException
import java.net.SocketTimeoutException

interface GenericResponseListener {
    fun onEventReceive(event: Event)
}

enum class TargetVar {
    VAR_GET_EVENTS, VAR_CREATE_PARTICIPANT, VAR_DELETE_PARTICIPANT
}

class EventsViewModel(application: Application): AndroidViewModel(application) {

    private val basic = Credentials.basic("gergokiss04@gmail.com", "asdasdasd")
    val genericResponseListener = MutableLiveData<GenericResponseListener>()

    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>>
        get() = _events

    fun modifyParticipation(eventId: Long, eventAccepted: Boolean) {
        if (eventAccepted)
            makeRequest(WebApi.retrofitService.deleteParticipantAsync(basic, eventId), TargetVar.VAR_DELETE_PARTICIPANT)
        else
            makeRequest(WebApi.retrofitService.createParticipantAsync(basic, eventId), TargetVar.VAR_CREATE_PARTICIPANT)
    }

    private fun getEvents() {
        makeRequest(WebApi.retrofitService.getEventsAsync(basic), TargetVar.VAR_GET_EVENTS)
    }

    private fun makeRequest(target: Deferred<GenericResponse>, targetVar: TargetVar) {
        viewModelScope.launch {
            try {
                val listResult = target.await()
                if (!listResult.error) {
                    when (targetVar) {
                        TargetVar.VAR_GET_EVENTS -> _events.value = listResult.events
                        TargetVar.VAR_CREATE_PARTICIPANT, TargetVar.VAR_DELETE_PARTICIPANT -> genericResponseListener.value!!.onEventReceive(listResult.event!!)
                    }
                }
                else handleResponseErrors(listResult.errors!!)
            }
            catch (e: Exception) {
                handleErrors(e)
            }
        }
    }

    private fun handleErrors(e: Exception) {
        val errors = when (e) {
            is SocketTimeoutException -> "Connection error! (server)"
            is ConnectException -> "Connection error! (client)"
            else -> e.message
        }
        Log.d("EventsViewModel", "Failure: $errors")
        Log.d("EventsViewModel", "$e")
        Toast.makeText(getApplication(), errors, Toast.LENGTH_LONG).show()
    }

    private fun handleResponseErrors(errors: List<String>) {
        var errorsMsg = ""
        Log.d("EventsViewModel", "Failure: ${errors.size} errors:")
        errors.forEachIndexed { index, e  ->
            run {
                Log.d("EventsViewModel", e)
                errorsMsg = errorsMsg.plus(if (index == 0) "" else "\n").plus(e)
            }
        }
        Toast.makeText(getApplication(), errorsMsg, Toast.LENGTH_LONG).show()
    }

    init {
        getEvents()
    }
}
