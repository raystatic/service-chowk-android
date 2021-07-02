package com.servicechowk.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.servicechowk.app.data.model.User
import com.servicechowk.app.data.repositories.HomeRepository
import com.servicechowk.app.other.Constants
import com.servicechowk.app.other.Resource
import com.servicechowk.app.other.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository
):ViewModel(){

    private val _providers = SingleLiveEvent<Resource<List<User>>>()
    val providers:SingleLiveEvent<Resource<List<User>>> get() = _providers

    fun getProviders(){
        _providers.postValue(Resource.loading(null))
        repository.getProviders().also {
            it.addSnapshotListener { value, error ->
                if (error == null){
                    val users = mutableListOf<User>()
                    if (value!=null && !value.isEmpty){
                        for (i in value.documents){
                            val user = i.toObject(User::class.java)
                            println("USERREMOTE: ${i}")

                            user?.let { it1 -> users.add(it1) }
                        }
                    }
                    _providers.postValue(Resource.success(users))
                }else{
                    _providers.postValue(Resource.error(Constants.SOMETHING_WENT_WRONG, null))

                }
            }
        }
    }

}