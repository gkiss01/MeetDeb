package com.gkiss01.meetdeb

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gkiss01.meetdeb.data.User
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {
    private val viewModelKoin: ActivityViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    companion object {
        lateinit var instance: MainActivity
    }

    init {
        instance = this
    }

    fun getActiveUser(): User? = viewModelKoin.activeUser.value?.data

//    private fun handleErrors(e: Exception) {
//        val errors = when (e) {
//            is SocketTimeoutException -> "Connection error! (server)"
//            is ConnectException -> "Connection error! (client)"
//            else -> e.message
//        }
//        Log.d("MainActivityApiCall", "Failure: $errors")
//        Log.d("MainActivityApiCall", "$e")
//        Toast.makeText(this, errors, Toast.LENGTH_LONG).show()
//    }
//
//    private fun handleResponseErrors(errorCode: ErrorCodes, errors: List<String>) {
//        var errorsMsg = ""
//        Log.d("MainActivityApiCall", "Failure: ${errors.size} errors:")
//        errors.forEachIndexed { index, e  ->
//            run {
//                Log.d("MainActivityApiCall", e)
//                errorsMsg = errorsMsg.plus(if (index == 0) "" else "\n").plus(e)
//            }
//        }
//        Toast.makeText(this, errorsMsg, Toast.LENGTH_LONG).show()
//    }
}
