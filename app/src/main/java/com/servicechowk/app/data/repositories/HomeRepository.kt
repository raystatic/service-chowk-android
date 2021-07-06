package com.servicechowk.app.data.repositories

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.servicechowk.app.data.model.HomeFilter
import com.servicechowk.app.data.model.Info
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val firestore: FirebaseFirestore
){


    fun addConfig(info: Info,deviceId:String): Task<Void> {
        return firestore.collection("appconfig")
                .document(deviceId)
                .set(info)
    }

    fun getProviders(homeFilter:HomeFilter):Query{
        var query = firestore.collection("users") as Query
        if (!homeFilter.field.isNullOrEmpty()){
            query = query.whereEqualTo("workField",homeFilter.field.toString())
            println("QUERYDEBUG: field $query")
        }

        if (!homeFilter.city.isNullOrEmpty()){
            query = query.whereEqualTo("city",homeFilter.city.toString())
            println("QUERYDEBUG: city $query")
        }

        if (!homeFilter.locality.isNullOrEmpty()){
            query = query.whereEqualTo("locality",homeFilter.locality.toString())
            println("QUERYDEBUG: locality $query")
        }

        return query
    }

    fun getLocalities():Query{
        return firestore.collection("localities")
    }

}