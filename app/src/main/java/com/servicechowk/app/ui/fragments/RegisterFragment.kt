package com.servicechowk.app.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.widget.ListPopupWindow
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.servicechowk.app.R
import com.servicechowk.app.data.model.User
import com.servicechowk.app.databinding.FragmentRegisterBinding
import com.servicechowk.app.other.Constants
import com.servicechowk.app.other.Extensions.showSnack
import com.servicechowk.app.other.Status
import com.servicechowk.app.other.Utility
import com.servicechowk.app.ui.viewmodels.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import javax.inject.Inject


@AndroidEntryPoint
class RegisterFragment: Fragment(R.layout.fragment_register) {

    private var _binding:FragmentRegisterBinding?=null
    private val binding:FragmentRegisterBinding get() = _binding!!

    private val vm by viewModels<UserViewModel>()

    private var newUser:User?=null

    @Inject
    lateinit var auth:FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRegisterBinding.bind(view)

        val user = auth.currentUser

        if (user == null) findNavController().navigateUp()

        initUI()

        subscribeToObservers()

        vm.getUserData(userId = user?.uid.toString())

    }

    private fun subscribeToObservers() {
        vm.userData.observe(viewLifecycleOwner, {
            binding.apply {
                when(it.status){
                    Status.SUCCESS -> {
                        val data = it.data
                        println("USERDEBUG: $data")
                        newUser = User(
                            id = auth.currentUser?.uid.toString()
                        )
                    }

                    Status.ERROR -> {
                        root.showSnack(it.message.toString())
                    }

                    Status.LOADING  -> {

                    }
                }
            }
        })
    }

    private fun initUI() {
        binding.apply {

            showListPopupWindow(listOf("Yes","No"),etEquipment)

           showListPopupWindow(Constants.categories.toList(),etWorkField)

            Utility.getJson(requireContext())?.let {
                val citiesList = mutableListOf<String>()

                val jsonObject = JSONObject(it)
                val jsonArray = jsonObject.getJSONArray("array")

                for (i in 0 until jsonArray.length()){
                    val obj = jsonArray.getJSONObject(i)
                    val city = obj.getString("name")
                    citiesList.add(city)
                }

                showListPopupWindow(citiesList,etCity)

            }



        }
    }

    private fun showListPopupWindow(list:List<String>, editText: EditText){
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
                editText.setText(list[position])
                dismiss()
            }
        }

        editText.setOnClickListener {
            listPopupWindow.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}