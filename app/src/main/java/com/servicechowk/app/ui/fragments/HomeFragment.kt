package com.servicechowk.app.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.servicechowk.app.R
import com.servicechowk.app.databinding.FragmentHomeBinding

class HomeFragment: Fragment(R.layout.fragment_home) {

    private var _binding:FragmentHomeBinding?=null
    private val binding:FragmentHomeBinding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        initUI()

        subscribeToObservers()

    }

    private fun subscribeToObservers() {

    }

    private fun initUI() {

        binding.apply {
            btnEnd.setOnClickListener {
                findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}