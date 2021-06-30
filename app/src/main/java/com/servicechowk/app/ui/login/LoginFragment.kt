package com.servicechowk.app.ui.login

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.servicechowk.app.R
import com.servicechowk.app.databinding.FragmentHomeBinding
import com.servicechowk.app.databinding.FragmentLoginBinding

class LoginFragment: Fragment(R.layout.fragment_login) {

    private var _binding: FragmentLoginBinding?=null
    private val binding: FragmentLoginBinding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentLoginBinding.bind(view)

        initUI()

        subscribeToObserver()

    }

    private fun subscribeToObserver() {

    }

    private fun initUI() {

        binding.apply {
            btnNext.setOnClickListener {
                val number = etPhone.text.toString()
                if (number.isEmpty()){
                    tvError.text = getString(R.string.empty_phone)
                    tvError.isVisible = true
                    return@setOnClickListener
                }

                if (number.length != 10){
                    tvError.text = getString(R.string.invalid_phone)
                    tvError.isVisible = true
                    return@setOnClickListener
                }



            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}