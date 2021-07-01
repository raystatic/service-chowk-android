package com.servicechowk.app.ui.fragments

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.servicechowk.app.R
import com.servicechowk.app.data.model.User
import com.servicechowk.app.databinding.ChooserDialogBinding
import com.servicechowk.app.databinding.FragmentRegisterBinding
import com.servicechowk.app.other.Constants
import com.servicechowk.app.other.Extensions.showSnack
import com.servicechowk.app.other.Extensions.showToast
import com.servicechowk.app.other.Extensions.validateIsNotError
import com.servicechowk.app.other.Status
import com.servicechowk.app.other.Utility
import com.servicechowk.app.other.sdk29AndUp
import com.servicechowk.app.ui.viewmodels.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.lang.Exception
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class RegisterFragment: Fragment(R.layout.fragment_register){

    private val TAG = "STORAGEDEBUG"

    private var _binding:FragmentRegisterBinding?=null
    private val binding:FragmentRegisterBinding get() = _binding!!


    private val vm by viewModels<UserViewModel>()

    private var newUser:User?=null

    @Inject
    lateinit var auth:FirebaseAuth

    @Inject
    lateinit var storage:FirebaseStorage

    private var readPermissionGranted = false
    private var writePermissionGranted = false
    private lateinit var permissionLauncher:ActivityResultLauncher<Array<String>>

    private var currentFileName = ""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val currentRef = storage.reference.child("images/${auth.currentUser?.phoneNumber}")
        val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()){
            if (writePermissionGranted){
                val saved = savePhotoToInternalStorage(bmp = it)
                if (!saved){
                    binding.root.showSnack("Couldn't Save Image!")
                    currentFileName = ""
                }else{
                    binding.root.showSnack("File saved")
                    lifecycleScope.launch {
                        val file = loadImageFromInternalStorage(currentFileName)
                        file?.let {
                            Log.d(TAG, "onCreate: File loaded: ${it.absolutePath} | ${it.path} | ${it.canonicalPath}}")
                            try {
                                val stream = FileInputStream(it)
                                vm.uploadFile(currentRef.child(currentFileName),stream)
                            }catch (e:Exception){
                                e.printStackTrace()
                                binding.root.showSnack("File not found.")
                            }
                        } ?: kotlin.run {
                            Log.d(TAG, "onCreate: File not found")
                        }
                    }
                    Log.d(TAG, "onCreate: output uri: $saved")
                }
            }else{
                requireContext().showToast("Permission not Granted to take save photo")
            }
        }

        vm.takePhoto.observe(requireActivity(),{
            it?.let {
                if (it){
                    takePhoto.launch()
                }
            }
        })

        vm.setTakePhoto(false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRegisterBinding.bind(view)

        val user = auth.currentUser

        if (user == null) findNavController().navigateUp()

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            readPermissionGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: readPermissionGranted
            writePermissionGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: writePermissionGranted

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                if (readPermissionGranted){
                    showChooserDialog()
                }else{
                    permissionNotGrantedDialog()
                }
            }else{
                if (readPermissionGranted && writePermissionGranted){
                    showChooserDialog()
                }else{
                    permissionNotGrantedDialog()
                }
            }

        }

        initUI()

        subscribeToObservers()

        vm.getUserData(userId = user?.uid.toString())

    }

    private fun permissionNotGrantedDialog(){
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.apply {
            setTitle("Permission not granted")
            setMessage("Please go to settings and allow permission")
            setPositiveButton("OK"){dialog,which ->
                dialog.cancel()
            }
        }

        alertDialog.create().show()
    }



    private fun subscribeToObservers() {

        vm.uploadedFileUrl.observe(viewLifecycleOwner, {
            binding.apply {
                when (it.status) {
                    Status.SUCCESS -> {
                        Log.d(TAG, "subscribeToObservers: success: ${it.data}")
                    }

                    Status.ERROR -> {
                        root.showSnack(it.message.toString())
                    }

                    Status.LOADING -> {

                    }
                }
            }
        })

        vm.userData.observe(viewLifecycleOwner, {
            binding.apply {
                when(it.status){
                    Status.SUCCESS -> {
                        val data = it.data
                        println("USERDEBUG: $data")
                        newUser = User(
                            id = auth.currentUser?.uid.toString()
                        )
                        val phone = auth.currentUser?.phoneNumber
                        etPhone.setText(phone?.substring(range = 3 until phone.length))
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

    private fun updateOrRequestPermissionOrShowDialog(){
        val hasReadPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        val hasWritePermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        readPermissionGranted = hasReadPermission
        writePermissionGranted = hasWritePermission || minSdk29

        val permissionsToRequest = mutableListOf<String>()
        if (!writePermissionGranted){
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (!readPermissionGranted){
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (permissionsToRequest.isNotEmpty()){
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }else{
            showChooserDialog()
        }


    }

    private suspend fun loadImageFromInternalStorage(name:String):File?{
        return withContext(Dispatchers.IO){
            val files = requireActivity().filesDir.listFiles()
            files?.find { it.canRead() && it.isFile && it.name == name }
        }
    }

    private fun savePhotoToInternalStorage(bmp: Bitmap):Boolean{
        return try {
            requireActivity().openFileOutput(currentFileName,MODE_PRIVATE).use { stream->
                if (!bmp.compress(Bitmap.CompressFormat.JPEG,100,stream)){
                    throw IOException("Couldn't save bitmap")
                }
            }
            true
        }catch (e:IOException){
            e.printStackTrace()
            false
        }
    }


    private fun showChooserDialog(){
        val dialog = Dialog(requireContext())
        val binding = ChooserDialogBinding.inflate(requireActivity().layoutInflater)
        dialog.setContentView(binding.root)

        binding.apply {

            tvCancel.setOnClickListener {
                dialog.cancel()
            }

            tvGallery.setOnClickListener {
                dialog.cancel()
                uploadPhotoFromGallery()
            }

            tvTakePhoto.setOnClickListener {
                dialog.cancel()
                vm.setTakePhoto(true)
            }

        }

        dialog.show()

//        val window = dialog.window
//        window?.setLayout(
//            WindowManager.LayoutParams.MATCH_PARENT,
//            WindowManager.LayoutParams.WRAP_CONTENT
//        )

    }

    private fun uploadPhotoFromGallery() {

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

            etAadhar.setOnClickListener {
                currentFileName = Constants.AADHAR_NAME
               updateOrRequestPermissionOrShowDialog()
            }

            etPhotoOfWork.setOnClickListener {
                currentFileName = Constants.WORK_PHOTO_NAME
                updateOrRequestPermissionOrShowDialog()
            }

            cardImg.setOnClickListener {
                currentFileName = Constants.PROFILE_NAME
                updateOrRequestPermissionOrShowDialog()
            }



            btnRegister.setOnClickListener {
                if (
                    etName.validateIsNotError("Name cannot be empty") &&
                            etWorkField.validateIsNotError("Please choose a work field") &&
                            etCity.validateIsNotError("Please choose a city") &&
                            etLocality.validateIsNotError("Please enter your locality") &&
                            etLastWorkAt.validateIsNotError("Empty last work place") &&
                            etAadhar.validateIsNotError("Aadhar card not added") &&
                            etPhotoOfWork.validateIsNotError("Work photo not added")
                ){
                    val name = etName.text.toString()
                    val category = etWorkField.text.toString()
                    val city = etCity.text.toString()
                    val locality = etLocality.text.toString()
                    val phoneNumber = auth.currentUser?.phoneNumber
                    val equipmentInput = etEquipment.text.toString()
                    val equipmentAvailable = equipmentInput == "Yes"
                    val education = etEducation.text.toString()
                    val lastWorkPlace = etLastWorkAt.text.toString()

                    root.showSnack("OKAY!")

                }else{
                    return@setOnClickListener
                }
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