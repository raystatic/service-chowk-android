package com.servicechowk.app.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val firestore: FirebaseFirestore
){

    fun getProviders():Query{
        return firestore.collection("users")
    }

}