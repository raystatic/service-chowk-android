package com.servicechowk.app.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.auth.User
import com.servicechowk.app.R
import com.servicechowk.app.databinding.FragmentHomeBinding
import com.servicechowk.app.other.Extensions.showSnack
import com.servicechowk.app.other.Status
import com.servicechowk.app.ui.adapters.ProvidersAdapter
import com.servicechowk.app.ui.viewmodels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment: Fragment(R.layout.fragment_home) {

    private var _binding:FragmentHomeBinding?=null
    private val binding:FragmentHomeBinding get() = _binding!!

    private val vm by viewModels<HomeViewModel>()

    private lateinit var providersAdapter: ProvidersAdapter

    @Inject
    lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        val user = auth.currentUser

        providersAdapter = ProvidersAdapter(
            onStartChat = {

            },
            onViewProfile = {
                val bundle = bundleOf(
                        "userId" to it.id
                )
                findNavController().navigate(R.id.action_homeFragment_to_providerProfileViewFragment,bundle)
            }
        )

        initUI(user)


        subscribeToObservers()

        vm.getProviders()

    }

    private fun subscribeToObservers() {
        vm.providers.observe(viewLifecycleOwner,{
           binding.apply {
               when(it.status){
                   Status.SUCCESS ->{
                       val list = it.data
                       rvProviders.isVisible = !list.isNullOrEmpty()
                       tvEmpty.isVisible = list.isNullOrEmpty()
                        it.data?.let {
                            providersAdapter.submitData(it)
                        }
                       progressbar.isVisible = false
                   }
                   Status.ERROR -> {
                       progressbar.isVisible = false
                        root.showSnack(it.message.toString())
                   }
                   Status.LOADING -> {
                       progressbar.isVisible = true
                   }
               }
           }
        })
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

            rvProviders.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = providersAdapter
            }

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}