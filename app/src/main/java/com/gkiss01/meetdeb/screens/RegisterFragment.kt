package com.gkiss01.meetdeb.screens

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.gkiss01.meetdeb.MainActivity
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.User
import com.gkiss01.meetdeb.data.request.UserRequest
import com.gkiss01.meetdeb.databinding.RegisterFragmentBinding
import com.gkiss01.meetdeb.network.moshi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class RegisterFragment : Fragment() {

    private lateinit var binding: RegisterFragmentBinding

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUserReceived(user: User) {
        Toast.makeText(context, "User created with id: ${user.id}", Toast.LENGTH_LONG).show()
        binding.registerButton.hideProgress(R.string.register_created)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.register_fragment, container, false)
        binding.registerButton.attachTextChangeAnimator()

        binding.registerButton.setOnClickListener {
            var error = false

            if (TextUtils.isEmpty(binding.email.text)) {
                binding.email.error = "A mezőt kötelező kitölteni!"
                error = true
            }
            else if (!Patterns.EMAIL_ADDRESS.matcher(binding.email.text).matches()) {
                binding.email.error = "Az email cím nem valódi!"
                error = true
            }

            if (TextUtils.isEmpty(binding.password.text)) {
                binding.password.error = "A mezőt kötelező kitölteni!"
                error = true
            }
            else if (binding.password.text.length < 8) {
                binding.password.error = "A jelszó min. 8 karakter lehet!"
                error = true
            }

            when {
                TextUtils.isEmpty(binding.name.text) -> {
                    binding.name.error = "A mezőt kötelező kitölteni!"
                    error = true
                }
                binding.name.text.length < 4 -> {
                    binding.name.error = "A név min. 4 karakter lehet!"
                    error = true
                }
                binding.name.text.length > 80 -> {
                    binding.name.error = "A név max. 80 karakter lehet!"
                    error = true
                }
            }

            if (!error) {
                binding.registerButton.showProgress {
                    buttonTextRes = R.string.register_create_waiting
                    progressColor = Color.WHITE
                }

                val inputMethodManager = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(view!!.windowToken, 0)

                val userRequest = UserRequest(binding.email.text.toString(), binding.password.text.toString(), binding.name.text.toString())
                val json = moshi.adapter(UserRequest::class.java).toJson(userRequest)
                val user = json.toRequestBody("application/json".toMediaTypeOrNull())

                MainActivity.instance.uploadUser(user)
            }
        }

        return binding.root
    }
}