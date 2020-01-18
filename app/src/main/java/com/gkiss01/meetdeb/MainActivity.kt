package com.gkiss01.meetdeb

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gkiss01.meetdeb.data.GenericResponse
import com.gkiss01.meetdeb.network.NavigationCode
import com.gkiss01.meetdeb.network.TargetVar
import com.gkiss01.meetdeb.network.WebApi
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import okhttp3.Credentials
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import java.net.ConnectException
import java.net.SocketTimeoutException

class MainActivity : AppCompatActivity() {

    private val basic = Credentials.basic("gergokiss04@gmail.com", "asdasdasd")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    companion object {
        lateinit var instance: MainActivity
    }

    init {
        instance = this
    }

    fun getEvents() {
        makeRequest(WebApi.retrofitService.getEventsAsync(basic), TargetVar.VAR_GET_EVENTS)
    }

    fun uploadEvent(event: RequestBody, image: MultipartBody.Part?) {
        makeRequest(WebApi.retrofitService.createEventAsync(basic, event, image), TargetVar.VAR_CREATE_EVENT)
    }

    fun modifyParticipation(eventId: Long, eventAccepted: Boolean) {
        if (eventAccepted)
            makeRequest(WebApi.retrofitService.deleteParticipantAsync(basic, eventId), TargetVar.VAR_DELETE_PARTICIPANT)
        else
            makeRequest(WebApi.retrofitService.createParticipantAsync(basic, eventId), TargetVar.VAR_CREATE_PARTICIPANT)
    }

    private fun makeRequest(target: Deferred<GenericResponse>, targetVar: TargetVar) {
        lifecycleScope.launch {
            try {
                val listResult = target.await()
                if (!listResult.error) {
                    when (targetVar) {
                        TargetVar.VAR_GET_EVENTS -> EventBus.getDefault().post(listResult.events)
                        TargetVar.VAR_CREATE_EVENT -> EventBus.getDefault().post(NavigationCode.NAVIGATE_TO_EVENTS_FRAGMENT)
                        TargetVar.VAR_CREATE_PARTICIPANT, TargetVar.VAR_DELETE_PARTICIPANT -> EventBus.getDefault().post(listResult.event)
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
        Toast.makeText(this, errors, Toast.LENGTH_LONG).show()
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
        Toast.makeText(this, errorsMsg, Toast.LENGTH_LONG).show()
    }
}
