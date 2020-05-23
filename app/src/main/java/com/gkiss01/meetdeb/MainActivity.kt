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
}
