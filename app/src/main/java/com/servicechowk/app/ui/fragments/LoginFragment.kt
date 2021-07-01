package com.servicechowk.app.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.servicechowk.app.R
import com.servicechowk.app.databinding.FragmentHomeBinding
import com.servicechowk.app.databinding.FragmentLoginBinding
import com.servicechowk.app.other.Extensions.showToast
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@AndroidEntryPoint
class LoginFragment: Fragment(R.layout.fragment_login) {

    private val TAG = "LOGINFRAGMENT"

    private var _binding: FragmentLoginBinding?=null
    private val binding: FragmentLoginBinding get() = _binding!!

    private var storedVerificationId = ""
    private var resendToken : PhoneAuthProvider.ForceResendingToken ?= null

    @Inject
    lateinit var auth:FirebaseAuth

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

            relOTP.isVisible = false
            linPhone.isVisible = true

            imgBack.setOnClickListener {
                findNavController().navigateUp()
            }

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

                sendOTP(number)

            }

            btnVerify.setOnClickListener {
                val otp = etOtp.text.toString()
                if (otp.isNotEmpty()){
                    if (storedVerificationId.isEmpty()){
                        requireContext().showToast(getString(R.string.please_try_again))
                        return@setOnClickListener
                    }
                    val credential = PhoneAuthProvider.getCredential(storedVerificationId, otp)
                    signInWithPhoneAuthCredential(credential)
                }else{
                    requireActivity().showToast("OTP cannot be empty")
                }
            }

        }

    }

    private fun sendOTP(number: String) {

        binding.tvInfo.text = "OTP sent to +91 $number"
        binding.linPhone.isVisible = false
        binding.relOTP.isVisible = true

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:$credential")
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)

                var message = ""

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    message = "Invalid Request"
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    message = "Quota for OTP exceeded"
                }


                requireContext().showToast(message)

                // Show a message and update the UI
            }

            override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
                resendToken = token
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber("+91$number")
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(requireActivity())
                .setCallbacks(callbacks)
                .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener {
                    if (it.isSuccessful){
                        requireContext().showToast("Login success")
                        val user = it.result?.user
                        Log.d(TAG, "signInWithPhoneAuthCredential: $user")
                        findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
                    }else{
                        Log.d(TAG, "signInWithPhoneAuthCredential: ${it.exception}")
                        if (it.exception is FirebaseAuthInvalidCredentialsException){
                            requireActivity().showToast("Incorrect OTP!")
                        }
                    }
                }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}