package com.servicechowk.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.auth.User
import com.google.firebase.storage.StorageReference
import com.servicechowk.app.data.repositories.UserRepository
import com.servicechowk.app.other.Constants
import com.servicechowk.app.other.Resource
import com.servicechowk.app.other.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.FileInputStream
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository
):ViewModel() {

    private val _takePhoto = SingleLiveEvent<Boolean>()
    val takePhoto:SingleLiveEvent<Boolean> get() = _takePhoto

    private val _userData = SingleLiveEvent<Resource<User>>()
    val userData:SingleLiveEvent<Resource<User>> get() = _userData

    private val _uploadedFileUrl = SingleLiveEvent<Resource<String>>()
    val uploadedFileUrl:SingleLiveEvent<Resource<String>> get() = _uploadedFileUrl

    fun uploadFile(storageReference: StorageReference,stream:FileInputStream){
        _uploadedFileUrl.postValue(Resource.loading(null))
        try {
            val uploadTask = repository.uploadFile(storageReference,stream)

            val urlTask = uploadTask.continueWithTask {
                if (!it.isSuccessful){
                    it.exception?.let {
                        throw  it
                    }
                }
                storageReference.downloadUrl
            }
            .addOnCompleteListener {
                if (it.isSuccessful){
                    _uploadedFileUrl.postValue(Resource.success(it.result.toString()))
                }else{
                    _uploadedFileUrl.postValue(Resource.error(Constants.SOMETHING_WENT_WRONG, null))
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
            _uploadedFileUrl.postValue(Resource.error(Constants.SOMETHING_WENT_WRONG,null))
        }
    }

    fun setTakePhoto(b:Boolean){
        _takePhoto.postValue(b)
    }

    fun getUserData(userId:String){
        _userData.postValue(Resource.loading(null))
        try{
            repository.getUser(userId)
                .addSnapshotListener { value, error ->
                    if (error == null){
                        if (value == null || value.isEmpty){
                            _userData.postValue(Resource.success(null))
                        }else{
                            _userData.postValue(Resource.success(value.documents[0].toObject(User::class.java)))
                        }
                    }else{
                        _userData.postValue(Resource.error(Constants.SOMETHING_WENT_WRONG,null))
                    }
                }
        }catch (e:Exception){
            e.printStackTrace()
            _userData.postValue(Resource.error(Constants.SOMETHING_WENT_WRONG, null))
        }
    }

}