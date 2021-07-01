package com.servicechowk.app.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.auth.User
import com.servicechowk.app.R
import com.servicechowk.app.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment: Fragment(R.layout.fragment_home) {

    private var _binding:FragmentHomeBinding?=null
    private val binding:FragmentHomeBinding get() = _binding!!

    @Inject
    lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        val user = auth.currentUser
        initUI(user)



        subscribeToObservers()

    }

    private fun subscribeToObservers() {

    }

    private fun initUI(user:FirebaseUser?) {

        binding.apply {

            if (user == null){
                btnEnd.text = "Login as Provider"
            }else{
                btnEnd.text ="Profile"
            }

            btnEnd.setOnClickListener {
                if (user == null) findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
                else findNavController().navigate(R.id.action_homeFragment_to_registerFragment)
            }



        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}