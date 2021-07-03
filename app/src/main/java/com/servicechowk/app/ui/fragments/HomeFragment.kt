package com.servicechowk.app.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.servicechowk.app.R
import com.servicechowk.app.data.model.HomeFilter
import com.servicechowk.app.databinding.FragmentHomeBinding
import com.servicechowk.app.other.Constants
import com.servicechowk.app.other.Extensions.showSnack
import com.servicechowk.app.other.Status
import com.servicechowk.app.other.Utility
import com.servicechowk.app.ui.adapters.LocalityFilterAdapter
import com.servicechowk.app.ui.adapters.ProvidersAdapter
import com.servicechowk.app.ui.viewmodels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment: Fragment(R.layout.fragment_home) {

    private var _binding:FragmentHomeBinding?=null
    private val binding:FragmentHomeBinding get() = _binding!!

    private val vm by viewModels<HomeViewModel>()

    private lateinit var providersAdapter: ProvidersAdapter
    private lateinit var providerLocalityFilterAdapter: LocalityFilterAdapter

    @Inject
    lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        val user = auth.currentUser

        providersAdapter = ProvidersAdapter(
            currentUserId = user?.uid,
            onStartChat = {
                val bundle = bundleOf(
                    "isConsumer" to true,
                    "providerId" to it.id,
                    "consumerId" to Utility.getDeviceId(requireContext()),
                    "providerFCMToken" to it.fcmToken
                )
                findNavController().navigate(R.id.action_homeFragment_to_chatFragment,bundle)
            },
            onViewProfile = {
                val bundle = bundleOf(
                        "userId" to it.id
                )
                findNavController().navigate(R.id.action_homeFragment_to_providerProfileViewFragment,bundle)
            },
            openMyProfile = {
                if (user == null) findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
                else findNavController().navigate(R.id.action_homeFragment_to_registerFragment)
            }
        )

        providerLocalityFilterAdapter = LocalityFilterAdapter {
            binding.etLocality.setText(it)
            binding.rvLocalityFilter.isVisible = false

            val currentFilter = vm.getCurrentFilter()
            currentFilter?.locality=  it
            currentFilter?.let { it1 -> vm.setHomeFilter(it1) }

        }

        initUI(user)


        subscribeToObservers()

        vm.getLocalities()

        vm.setHomeFilter(HomeFilter(null,null,null))

    }

    private fun subscribeToObservers() {

        vm.localities.observe(viewLifecycleOwner,{
            binding.apply {
                when(it.status){
                    Status.SUCCESS ->{

                    }
                    Status.ERROR -> {

                    }
                    Status.LOADING -> {

                    }
                }
            }
        })


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

            val list = mutableListOf<String>("Select")
            list.addAll(Constants.categories.toList())

            showListPopupWindow(list,tvWorkField,1)

            Utility.getJson(requireContext())?.let {
                val citiesList = mutableListOf<String>()

                val jsonObject = JSONObject(it)
                val jsonArray = jsonObject.getJSONArray("array")

                for (i in 0 until jsonArray.length()){
                    val obj = jsonArray.getJSONObject(i)
                    val city = obj.getString("name")
                    citiesList.add(city)
                }

                val myCitiesList = mutableListOf<String>("Select")
                myCitiesList.addAll(citiesList)

                showListPopupWindow(myCitiesList,tvCity,2)

            }

            etLocality.doAfterTextChanged {
                val text = it.toString()
                rvLocalityFilter.isVisible =text.isNotEmpty()
            }

            rvLocalityFilter.isVisible = false

            rvLocalityFilter.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = providerLocalityFilterAdapter
            }


        }

    }

    private fun showListPopupWindow(list:List<String>, textView: TextView, filterType:Int){
        val listPopupWindow = ListPopupWindow(requireContext())
        listPopupWindow.apply {
            setAdapter(
                    ArrayAdapter(
                            requireContext(),
                            R.layout.item_request_category,
                            list
                    )
            )
            anchorView = textView
            isModal=  true

            setOnItemClickListener { parent, view, position, id ->
                textView.text = list[position]
                val currentFilter = vm.getCurrentFilter()
                when(filterType){
                    1 -> {
                        currentFilter?.field = list[position]
                    }
                    2-> {
                        currentFilter?.city = list[position]
                    }
                }
                currentFilter?.let { vm.setHomeFilter(it) }
                dismiss()
            }
        }

        textView.setOnClickListener {
            listPopupWindow.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}