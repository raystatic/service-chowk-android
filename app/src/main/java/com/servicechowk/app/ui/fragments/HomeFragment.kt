package com.servicechowk.app.ui.fragments

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.servicechowk.app.R
import com.servicechowk.app.data.model.HomeFilter
import com.servicechowk.app.data.model.Info
import com.servicechowk.app.data.model.ProviderLocality
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

    private var _binding: FragmentHomeBinding?=null
    private val binding: FragmentHomeBinding get() = _binding!!

    private val vm by viewModels<HomeViewModel>()

    private lateinit var providersAdapter: ProvidersAdapter
    private lateinit var providerLocalityFilterAdapter: LocalityFilterAdapter

    @Inject
    lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        try {
            val pInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            val version = pInfo.versionName
            val info = Info(
                    version = version
            )
            vm.addConfig(
                    info = info,
                    deviceId = Utility.getDeviceId(requireContext())
            )
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            Log.d("onViewCreated: ", "Cannot update version")
        }

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
                    findNavController().navigate(R.id.action_homeFragment_to_chatFragment, bundle)
                },
                onViewProfile = {
                    val bundle = bundleOf(
                            "userId" to it.id
                    )
                    findNavController().navigate(R.id.action_homeFragment_to_providerProfileViewFragment, bundle)
                },
                openMyProfile = {
                    if (user == null) findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
                    else findNavController().navigate(R.id.action_homeFragment_to_registerFragment)
                }
        )

        initUI(user)


        subscribeToObservers()

        vm.getLocalities()

        vm.setHomeFilter(HomeFilter(null, null, null))

    }

    private fun subscribeToObservers() {

        vm.localities.observe(viewLifecycleOwner, {
            binding.apply {
                when (it.status) {
                    Status.SUCCESS -> {
                        it.data?.let {
                            println("LOCALITYDEBUG: $it")
                            // showListPopupWindowForLocality(it.map { it.value },etLocality)
                            providerLocalityFilterAdapter = LocalityFilterAdapter(
                                    onClick = {
                                        binding.etLocality.setText(it)
                                        binding.etLocality.setSelection(binding.etLocality.text.toString().length)

                                        val currentFilter = vm.getCurrentFilter()
                                        currentFilter?.locality = it
                                        currentFilter?.let { it1 -> vm.setHomeFilter(it1) }
                                    },
                                    currentList = it as ArrayList<ProviderLocality>
                            )

                            rvLocalityFilter.apply {
                                layoutManager = LinearLayoutManager(requireContext())
                                adapter = providerLocalityFilterAdapter
                            }

                            etLocality.doAfterTextChanged {
                                providerLocalityFilterAdapter.filter.filter(it.toString())
                                if (it.toString().isEmpty()) {
                                    val currentFilter = vm.getCurrentFilter()
                                    currentFilter?.locality = null
                                    currentFilter?.let { it1 -> vm.setHomeFilter(it1) }
                                }

                            }
                        }
                    }
                    Status.ERROR -> {

                    }
                    Status.LOADING -> {

                    }
                }
            }
        })


        vm.providers.observe(viewLifecycleOwner, {
            binding.apply {
                when (it.status) {
                    Status.SUCCESS -> {
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

    private fun initUI(user: FirebaseUser?) {

        binding.apply {

            if (user == null){
                btnEnd.text = "Login as Provider"
                btnEnd.isVisible = true
                imgProfile.isVisible  = false
            }else{

                btnEnd.text ="My Profile"
                println("PROFILEDEBUG: ${user.photoUrl}")
                if (user.photoUrl.toString().isEmpty()){
                    btnEnd.isVisible = true
                    imgProfile.isVisible  = false
                }else{
                    btnEnd.isVisible = false
                    imgProfile.isVisible  = true
                    Glide.with(requireContext())
                        .load(user.photoUrl.toString())
                        .placeholder(R.drawable.person)
                        .error(R.drawable.person)
                        .into(imgProfile)
                }

            }

            btnEnd.setOnClickListener {
                if (user == null) findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
                else findNavController().navigate(R.id.action_homeFragment_to_registerFragment)
            }

            imgProfile.setOnClickListener {
                if (user == null) findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
                else findNavController().navigate(R.id.action_homeFragment_to_registerFragment)
            }

//            rvLocalityFilter.apply {
//                layoutManager = LinearLayoutManager(requireContext())
//                adapter = providerLocalityFilterAdapter
//            }

            rvProviders.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = providersAdapter
            }

            val list = mutableListOf<String>("Select Category")
            list.addAll(Constants.categories.toList().sortedBy { it })

            showListPopupWindow(list, tvWorkField, 1)

            Utility.getJson(requireContext())?.let {
                val citiesList = mutableListOf<String>()

                val jsonObject = JSONObject(it)
                val jsonArray = jsonObject.getJSONArray("array")

                for (i in 0 until jsonArray.length()){
                    val obj = jsonArray.getJSONObject(i)
                    val city = obj.getString("name")
                    citiesList.add(city)
                }

                val myCitiesList = mutableListOf<String>("Select City")
                myCitiesList.addAll(citiesList)

                showListPopupWindow(myCitiesList.sortedBy { it }, tvCity, 2)

            }


//            etLocality.doAfterTextChanged {
//                val text = it.toString()
//                rvLocalityFilter.isVisible =text.isNotEmpty()
//
//                if(text.isEmpty()){
//                    val currentFilter = vm.getCurrentFilter()
//                    currentFilter?.locality=  null
//                    currentFilter?.let { it1 -> vm.setHomeFilter(it1) }
//                }
//            }
            rvLocalityFilter.isVisible = true

           // rvLocalityFilter.isVisible = false

        }

    }

    private fun showListPopupWindowForLocality(list: List<String>, editText: EditText){
        val listPopupWindow = ListPopupWindow(requireContext())
        listPopupWindow.apply {
            setAdapter(
                    ArrayAdapter(
                            requireContext(),
                            R.layout.item_request_category,
                            list
                    )
            )
            anchorView = editText
            isModal=  true

            setOnItemClickListener { parent, view, position, id ->
                val currentFilter = vm.getCurrentFilter()
                editText.setText(list[position])
                currentFilter?.locality = list[position]
                currentFilter?.let { vm.setHomeFilter(it) }
                dismiss()
            }
        }

        editText.setOnClickListener {
            listPopupWindow.show()
        }
    }


    private fun showListPopupWindow(list: List<String>, textView: TextView, filterType: Int){
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
                val currentFilter = vm.getCurrentFilter()
                if (position == 0){
                    textView.hint = "Select"

                    when(filterType){
                        1 -> {
                            currentFilter?.field = null
                            textView.text = "Select Category"
                        }
                        2 -> {
                            currentFilter?.city = null
                            textView.text = "Select City"
                        }
                    }
                    currentFilter?.let { vm.setHomeFilter(it) }
                }else{
                    textView.text = list[position]
                    when(filterType){
                        1 -> {
                            currentFilter?.field = list[position]
                        }
                        2 -> {
                            currentFilter?.city = list[position]
                        }
                    }
                    currentFilter?.let { vm.setHomeFilter(it) }
                }
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