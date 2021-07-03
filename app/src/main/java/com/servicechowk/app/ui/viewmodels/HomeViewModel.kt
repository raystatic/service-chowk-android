package com.servicechowk.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.servicechowk.app.data.model.HomeFilter
import com.servicechowk.app.data.model.ProviderLocality
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

    private val _homeFilter = SingleLiveEvent<HomeFilter>()

    private val _localHomeFilter = SingleLiveEvent<HomeFilter>()


    private val _providers = SingleLiveEvent<Resource<List<User>>>()
   // val providers:SingleLiveEvent<Resource<List<User>>> get() = _providers

    private val _localities = SingleLiveEvent<Resource<List<ProviderLocality>>>()
    val localities:SingleLiveEvent<Resource<List<ProviderLocality>>> get() = _localities

    val providers = _homeFilter.switchMap {
        _providers.postValue(Resource.loading(null))
        repository.getProviders(it).also {
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
        _providers
    }

    fun getCurrentFilter() = _homeFilter.value

    fun setHomeFilter(homeFilter: HomeFilter){
        _homeFilter.postValue(homeFilter)
    }

    fun getLocalities(){
        _localities.postValue(Resource.loading(null))
        repository.getLocalities().also {
            it.addSnapshotListener { value, error ->
                if (error == null){
                    val localities = mutableListOf<ProviderLocality>()
                    if (value!=null && !value.isEmpty){
                        for (i in value.documents){
                            val locality = i.toObject(ProviderLocality::class.java)

                            locality?.let { it1 -> localities.add(it1) }
                        }
                    }
                    _localities.postValue(Resource.success(localities))
                }else{
                    _localities.postValue(Resource.error(Constants.SOMETHING_WENT_WRONG,null))
                }
            }
        }
    }

    fun getProviders(){

    }

}