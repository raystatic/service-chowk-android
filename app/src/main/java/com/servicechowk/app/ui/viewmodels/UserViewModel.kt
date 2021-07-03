package com.servicechowk.app.ui.viewmodels

import android.net.Uri
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

    private val _openGallery = SingleLiveEvent<Boolean>()
    val openGallery:SingleLiveEvent<Boolean> get() = _openGallery

    private val _userData = SingleLiveEvent<Resource<com.servicechowk.app.data.model.User>>()
    val userData:SingleLiveEvent<Resource<com.servicechowk.app.data.model.User>> get() = _userData

    private val _uploadedFileUrl = SingleLiveEvent<Resource<Pair<String,String>>>()
    val uploadedFileUrl:SingleLiveEvent<Resource<Pair<String,String>>> get() = _uploadedFileUrl

    private val _addingUser = SingleLiveEvent<Resource<Boolean>>()
    val addingUser:SingleLiveEvent<Resource<Boolean>> get() = _addingUser


    fun addUser(user: com.servicechowk.app.data.model.User){
        _addingUser.postValue(Resource.loading(null))
        repository.addUser(user)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    _addingUser.postValue(Resource.success(true))
                    repository.addLocality(user.locality.toString(),user.id)
                }else{
                    _addingUser.postValue(Resource.error(Constants.SOMETHING_WENT_WRONG, null))
                }
            }
    }

    fun uploadFileByUri(storageReference: StorageReference,uri:Uri, currentFileName:String){
        _uploadedFileUrl.postValue(Resource.loading(null))
        try {
            val uploadTask = repository.uploadFileByUri(storageReference,uri)

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
                            _uploadedFileUrl.postValue(Resource.success(Pair(it.result.toString(),currentFileName)))
                        }else{
                            _uploadedFileUrl.postValue(Resource.error(Constants.SOMETHING_WENT_WRONG, null))
                        }
                    }
        }catch (e:Exception){
            e.printStackTrace()
            _uploadedFileUrl.postValue(Resource.error(Constants.SOMETHING_WENT_WRONG,null))
        }
    }


    fun uploadFile(storageReference: StorageReference,stream:FileInputStream, currentFileName:String){
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
                    _uploadedFileUrl.postValue(Resource.success(Pair(it.result.toString(),currentFileName)))
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

    fun setOpenGallery(b:Boolean){
        _openGallery.postValue(b)
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
                            _userData.postValue(Resource.success(value.documents[0].toObject(com.servicechowk.app.data.model.User::class.java)))
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