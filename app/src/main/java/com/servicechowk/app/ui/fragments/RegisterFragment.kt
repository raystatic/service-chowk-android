package com.servicechowk.app.ui.fragments

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.servicechowk.app.R
import com.servicechowk.app.data.model.User
import com.servicechowk.app.databinding.ChooserDialogBinding
import com.servicechowk.app.databinding.ConfirmationDialogBinding
import com.servicechowk.app.databinding.FragmentRegisterBinding
import com.servicechowk.app.databinding.UploadingDialogBinding
import com.servicechowk.app.other.Constants
import com.servicechowk.app.other.Extensions.showSnack
import com.servicechowk.app.other.Extensions.showToast
import com.servicechowk.app.other.Extensions.validateIsNotError
import com.servicechowk.app.other.PrefManager
import com.servicechowk.app.other.Status
import com.servicechowk.app.other.Utility
import com.servicechowk.app.ui.MainActivity
import com.servicechowk.app.ui.viewmodels.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.lang.Exception
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class RegisterFragment: Fragment(R.layout.fragment_register){

    private val TAG = "STORAGEDEBUG"

    private var _binding: FragmentRegisterBinding?=null
    private val binding:FragmentRegisterBinding get() = _binding!!


    private val vm by viewModels<UserViewModel>()

    private var newUser:User?=null

    @Inject
    lateinit var auth:FirebaseAuth

    @Inject
    lateinit var storage:FirebaseStorage

    @Inject
    lateinit var prefManager: PrefManager

    private var readPermissionGranted = false
    private var writePermissionGranted = false
    private lateinit var permissionLauncher:ActivityResultLauncher<Array<String>>

    private var currentFileName = ""

    private lateinit var uploading:Dialog
    private lateinit var uploadingDialogBinding:UploadingDialogBinding

    private var profileUrl = ""
    private var aadharUrl = ""
    private var workPhotoUrl = ""

    private var changesMade = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                showConfirmationDialog()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(callback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkPermission()

        val currentRef = storage.reference.child("images/${auth.currentUser?.phoneNumber}")

        val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { vm.uploadFileByUri(currentRef.child(currentFileName), it,currentFileName) }
        }

        val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()){
            if (it!=null){
                println("SDKDEBUG: check $writePermissionGranted")
                if (writePermissionGranted){
                    val saved = savePhotoToInternalStorage(bmp = it)
                    if (!saved){
                        binding.root.showSnack("Couldn't Save Image!")
                        currentFileName = ""
                    }else{
                        lifecycleScope.launch {
                            val file = loadImageFromInternalStorage(currentFileName)
                            file?.let {
                                Log.d(TAG, "onCreate: File loaded: ${it.absolutePath} | ${it.path} | ${it.canonicalPath}}")
                                try {
                                    val stream = FileInputStream(it)
                                    vm.uploadFile(currentRef.child(currentFileName),stream,currentFileName)
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
        }

        vm.takePhoto.observe(requireActivity(),{
            it?.let {
                if (it){
                    takePhoto.launch()
                }
            }
        })

        vm.openGallery.observe(requireActivity(),{
            it?.let {
                if (it){
                    getContent.launch("image/*")
                }
            }
        })

        vm.setTakePhoto(false)
        vm.setOpenGallery(false)

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

        subscribeToObservers(user)

        vm.upadateToken(prefManager.getString(Constants.FCM_TOKEN).toString(),userId = user?.uid.toString())

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



    private fun subscribeToObservers(user:FirebaseUser?) {

        vm.updateToken.observe(viewLifecycleOwner, {
            binding.apply {
                when (it.status) {
                    Status.SUCCESS -> {
                        if (it.data != null && it.data) {
                            Log.d(TAG, "subscribeToObservers: FCM TOKEN UPDATED")
                        } else {
                            Log.d(TAG, "subscribeToObservers: FCM TOKEN NOT UPDATED")
                        }
                        vm.getUserData(userId = user?.uid.toString())
                    }

                    Status.ERROR -> {
                        Log.d(TAG, "subscribeToObservers: FCM TOKEN NOT UPDATED")
                    }

                    Status.LOADING -> {
                        showProgressDialog("Loading Data...")
                    }
                }
            }
        })


        vm.addingUser.observe(viewLifecycleOwner,{
           binding.apply {
               when(it.status){
                   Status.SUCCESS -> {
                        if (it.data!=null && it.data){
                            root.showSnack("User updated successfully")

                        }else{
                            root.showSnack(Constants.SOMETHING_WENT_WRONG)
                        }
                       changesMade = false
                       if (uploading.isShowing) uploading.cancel()
                   }

                   Status.ERROR -> {
                       if (uploading.isShowing) uploading.cancel()
                       root.showSnack(it.message.toString())
                   }

                   Status.LOADING -> {
                       showProgressDialog("Adding user...")
                   }
               }
           }
        })

        vm.uploadedFileUrl.observe(viewLifecycleOwner, {
            binding.apply {
                when (it.status) {
                    Status.SUCCESS -> {
                        it.data?.let {
                            when(it.second){
                                Constants.AADHAR_NAME -> {
                                    changesMade = aadharUrl != it.first
                                    etAadhar.isVisible = false
                                    relAadhar.isVisible = true
                                    aadharUrl = it.first
                                    Glide.with(requireContext())
                                        .load(aadharUrl)
                                        .into(imgAadhar)
                                }
                                Constants.WORK_PHOTO_NAME -> {
                                    changesMade = workPhotoUrl != it.first
                                    etPhotoOfWork.isVisible = false
                                    relWorkPhoto.isVisible = true
                                    workPhotoUrl = it.first
                                    Glide.with(requireContext())
                                        .load(workPhotoUrl)
                                        .into(imgWorkPhoto)
                                }
                                Constants.PROFILE_NAME -> {
                                    println("URLDEBUG: $profileUrl | ${it.first}")
                                    changesMade = profileUrl != it.first
                                    println("URLDEBUG: changes: $changesMade")
                                    profileUrl = it.first
                                    Glide.with(requireContext())
                                        .load(profileUrl)
                                        .placeholder(R.drawable.person)
                                        .error(R.drawable.person)
                                        .into(imgProfile)
                                }
                                else -> {

                                }
                            }
                        } ?: kotlin.run {
                            root.showSnack(Constants.SOMETHING_WENT_WRONG)
                        }
                        if (uploading.isShowing) uploading.cancel()
                    }

                    Status.ERROR -> {
                        if (uploading.isShowing) uploading.cancel()
                        root.showSnack(it.message.toString())
                    }

                    Status.LOADING -> {
                        showProgressDialog("Uploading File. Please wait..")
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
                        setUserData(data)
                        newUser = User(
                            id = auth.currentUser?.uid.toString(),
                            isVerified = data?.isVerified ?: false,
                            fcmToken = prefManager.getString(Constants.FCM_TOKEN)
                        )
                        val phone = auth.currentUser?.phoneNumber
                        etPhone.setText(phone?.substring(range = 3 until phone.length))
                        if (uploading.isShowing) uploading.cancel()
                        changesMade = false
                        onbserveDataForChanges(data)
                    }

                    Status.ERROR -> {
                        if (uploading.isShowing) uploading.cancel()
                        root.showSnack(it.message.toString())
                    }

                    Status.LOADING  -> {
//                        showProgressDialog("Loading Data...")
                    }
                }
            }
        })
    }

    private fun onbserveDataForChanges(user: User?) {
        binding.apply {
            etName.doAfterTextChanged {
                changesMade = user?.name != it.toString()
            }
            etWorkField.doAfterTextChanged {
                changesMade = user?.workField != it.toString()
            }
            etCity.doAfterTextChanged {
                changesMade = user?.city != it.toString()
            }
            etLocality.doAfterTextChanged {
                changesMade = user?.locality != it.toString()
            }
            etPhone.doAfterTextChanged {
                changesMade = user?.contactNumber != it.toString()
            }
            etEquipment.doAfterTextChanged {
                val equipment = it.toString() == "Yes"
                changesMade = user?.equipmentAvailable != equipment
            }
            etLastWorkAt.doAfterTextChanged {
                changesMade = user?.lastWorkAt != it.toString()
            }
            etEducation.doAfterTextChanged {
                changesMade = user?.education != it.toString()
            }

            changesMade = user?.photoId != aadharUrl
            changesMade = user?.workPhoto != workPhotoUrl
        }
    }

    private fun setUserData(user: User?) {
        binding.apply {
            user?.let { user1 ->

                btnChatRoom.isVisible = true

                btnChatRoom.setOnClickListener {
                    if (changesMade){
                        showConfirmationDialog()
                    }else {
                        val bundle = bundleOf(
                                "providerId" to auth.currentUser?.uid,
                                "providerFCMToken" to user1.fcmToken
                        )
                        findNavController().navigate(R.id.action_registerFragment_to_chatRoomFragment, bundle)
                    }
                }

                Glide.with(requireContext())
                    .load(user1.photo)
                    .placeholder(R.drawable.person)
                    .error(R.drawable.person)
                    .into(imgProfile)

                tvVerified.isVisible = user1.isVerified == true

                workPhotoUrl = user1.workPhoto.toString()
                aadharUrl = user1.photoId.toString()
                profileUrl = user1.photo.toString()

                etAadhar.isVisible = user1.photoId.isNullOrEmpty()
                relAadhar.isVisible = !user1.photoId.isNullOrEmpty()

                etPhotoOfWork.isVisible = user1.workPhoto.isNullOrEmpty()
                relWorkPhoto.isVisible = !user1.workPhoto.isNullOrEmpty()

                Glide.with(requireContext())
                    .load(user1.photoId)
                    .into(imgAadhar)

                Glide.with(requireContext())
                    .load(user1.workPhoto)
                    .into(imgWorkPhoto)

                etName.setText(user1.name)
                etWorkField.setText(user1.workField)
                etCity.setText(user1.city)
                etLocality.setText(user1.locality)
                val equipment = if (user1.equipmentAvailable == true) "Yes" else "No"
                etEquipment.setText(equipment)
                etEducation.setText(user1.education)
                etLastWorkAt.setText(user1.lastWorkAt)

            }
        }
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

    private fun checkPermission(){
        val hasReadPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        val hasWritePermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        println("SDKDEBUG: $minSdk29")

        readPermissionGranted = hasReadPermission
        writePermissionGranted = hasWritePermission || minSdk29
    }

    private fun updateOrRequestPermissionOrShowDialog(){


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

    }

    private fun uploadPhotoFromGallery() {

        vm.setOpenGallery(true)

//        val intent = Intent()
//        intent.type = "image/*"
//        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
//        intent.action = Intent.ACTION_GET_CONTENT
//        requireActivity().startActivityForResult(Intent.createChooser(intent, "Select Picture"), 101)
    }

    private fun initUI() {

        initUploadingDialog()

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

            relAadhar.setOnClickListener {
                currentFileName = Constants.AADHAR_NAME
                updateOrRequestPermissionOrShowDialog()
            }

            relWorkPhoto.setOnClickListener {
                currentFileName = Constants.WORK_PHOTO_NAME
                updateOrRequestPermissionOrShowDialog()
            }

            imgProfile.setOnClickListener {
                currentFileName = Constants.PROFILE_NAME
                updateOrRequestPermissionOrShowDialog()
            }


            imgBack.setOnClickListener {
                if (changesMade){
                    showConfirmationDialog()
                }else{
                    findNavController().navigateUp()
                }
            }


            btnRegister.setOnClickListener {
                if (
                    etName.validateIsNotError("Name cannot be empty") &&
                            etWorkField.validateIsNotError("Please choose a work field") &&
                            etCity.validateIsNotError("Please choose a city") &&
                            etLocality.validateIsNotError("Please enter your locality")
                ){
                    val userName = etName.text.toString()
                    val userCategory = etWorkField.text.toString()
                    val userCity = etCity.text.toString()
                    val userLocality = etLocality.text.toString()
                    val userPhoneNumber = auth.currentUser?.phoneNumber
                    val equipmentInput = etEquipment.text.toString()
                    val userEquipmentAvailable = equipmentInput == "Yes"
                    val userEducation = etEducation.text.toString()
                    val lastWorkPlace = etLastWorkAt.text.toString()

                    newUser?.apply {
                        name = userName
                        workField = userCategory
                        city = userCity
                        locality=  userLocality
                        contactNumber = userPhoneNumber
                        equipmentAvailable = userEquipmentAvailable
                        education = userEducation
                        lastWorkAt = lastWorkPlace
                        photoId  = aadharUrl
                        workPhoto = workPhotoUrl
                        photo = profileUrl
                    }

                    newUser?.let { it1 -> vm.addUser(it1) }

                    val profileUpdates = userProfileChangeRequest {
                        photoUri = Uri.parse(newUser?.photo)
                    }

                    auth.currentUser?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener {
                            if (it.isSuccessful){
                                Log.d(TAG, "initUI: Firebase user profile photo updated")
                            }else{
                                Log.d(TAG, "initUI: Firebase user profile photo not updated")
                            }
                        }

                }else{
                    return@setOnClickListener
                }
            }

            btnSignOut.setOnClickListener {
                auth.signOut()
                startActivity(Intent(requireActivity(),MainActivity::class.java))
                requireActivity().finish()
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

    private fun initUploadingDialog(){
        uploading = Dialog(requireContext())
        uploadingDialogBinding = UploadingDialogBinding.inflate(requireActivity().layoutInflater)
        uploading.setContentView(uploadingDialogBinding.root)
        uploading.setCancelable(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }



    private fun showConfirmationDialog() {
        val dialog = Dialog(requireContext())
        val dialogBinding = ConfirmationDialogBinding.inflate(requireActivity().layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(false)

        dialogBinding.apply {
            tvDiscard.setOnClickListener {
                dialog.cancel()
                changesMade = false
                onDestroyView()
            }

            tvSave.setOnClickListener {
                val userName = binding.etName.text.toString()
                val userCategory = binding.etWorkField.text.toString()
                val userCity = binding.etCity.text.toString()
                val userLocality = binding.etLocality.text.toString()
                val userPhoneNumber = auth.currentUser?.phoneNumber
                val equipmentInput = binding.etEquipment.text.toString()
                val userEquipmentAvailable = equipmentInput == "Yes"
                val userEducation = binding.etEducation.text.toString()
                val lastWorkPlace = binding.etLastWorkAt.text.toString()

                newUser?.apply {
                    name = userName
                    workField = userCategory
                    city = userCity
                    locality=  userLocality
                    contactNumber = userPhoneNumber
                    equipmentAvailable = userEquipmentAvailable
                    education = userEducation
                    lastWorkAt = lastWorkPlace
                    photoId  = aadharUrl
                    workPhoto = workPhotoUrl
                    photo = profileUrl
                }

                newUser?.let { it1 -> vm.addUser(it1) }
                changesMade = false
                dialog.dismiss()
            }

        }

        dialog.show()

        val window = dialog.window
        window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
        )

    }

}