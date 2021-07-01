package com.servicechowk.app.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.servicechowk.app.R
import com.servicechowk.app.data.model.User
import com.servicechowk.app.databinding.FragmentProviderProfileViewBinding
import com.servicechowk.app.databinding.UploadingDialogBinding
import com.servicechowk.app.other.Extensions.showSnack
import com.servicechowk.app.other.Status
import com.servicechowk.app.ui.viewmodels.UserViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ProviderProfileViewFragment: Fragment(R.layout.fragment_provider_profile_view) {

    private var _binding: FragmentProviderProfileViewBinding?=null
    private val binding: FragmentProviderProfileViewBinding get() = _binding!!

    private lateinit var uploading: Dialog
    private lateinit var uploadingDialogBinding: UploadingDialogBinding

    private var userId = ""

    private val vm by viewModels<UserViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProviderProfileViewBinding.bind(view)

        userId = arguments?.getString("userId") ?: ""
        if (userId.isEmpty()) findNavController().navigateUp()

        initUI()

        subscribeToObservers()

        vm.getUserData(userId)

    }

    private fun subscribeToObservers() {
        vm.userData.observe(viewLifecycleOwner, {
            binding.apply {
                when(it.status){
                    Status.SUCCESS -> {
                        val data = it.data
                        setUserData(data)
                        if (uploading.isShowing) uploading.cancel()
                    }

                    Status.ERROR -> {
                        if (uploading.isShowing) uploading.cancel()
                        root.showSnack(it.message.toString())
                    }

                    Status.LOADING  -> {
                        showProgressDialog("Loading Data...")
                    }
                }
            }
        })

    }

    private fun setUserData(data: User?) {
        binding.apply {
            data?.let {
                tvVerified.isVisible = it.isVerified == true
                Glide.with(requireContext())
                        .load(it.photo)
                        .placeholder(R.drawable.person)
                        .error(R.drawable.person)
                        .into(imgProfile)
                tvName.text = "Name: ${it.name}"
                tvWorkField.text = "Work Field: ${it.workField}"
                tvCity.text = "City: ${it.city}"
                tvLocality.text = "Locality: ${it.locality}"
                tvPhone.text = "Contact: ${it.contactNumber}"
                val available = if (it.equipmentAvailable == true) "Yes" else "No"
                tvEquipment.text = "Equipment available: $available"
                tvEducation.isVisible = !it.education.isNullOrEmpty()
                tvEducation.text = "Education: ${it.education}"
                tvLastWorkAt.text= "Last work at: ${it.lastWorkAt}"
            }
        }
    }

    private fun initUI() {
        initUploadingDialog()
        binding.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun initUploadingDialog(){
        uploading = Dialog(requireContext())
        uploadingDialogBinding = UploadingDialogBinding.inflate(requireActivity().layoutInflater)
        uploading.setContentView(uploadingDialogBinding.root)
    }

    private fun showProgressDialog(message:String) {
        uploadingDialogBinding.tvInfo.text = message
        uploading.show()
        val window = uploading.window
        window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
        )
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}