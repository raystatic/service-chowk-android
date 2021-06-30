package com.servicechowk.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.auth.User
import com.servicechowk.app.data.repositories.UserRepository
import com.servicechowk.app.other.Constants
import com.servicechowk.app.other.Resource
import com.servicechowk.app.other.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository
):ViewModel() {

    private val _userData = SingleLiveEvent<Resource<User>>()
    val userData:SingleLiveEvent<Resource<User>> get() = _userData

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