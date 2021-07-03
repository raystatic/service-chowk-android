package com.servicechowk.app.data.repositories

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.servicechowk.app.data.model.User
import java.io.FileInputStream
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    fun addLocality(locality:String, userId: String): Task<Void> {
        return firestore.collection("localities")
                .document(userId)
                .set(Pair("locality",locality))
    }

    fun addUser(user: User): Task<Void> {
        return firestore.collection("users")
            .document(user.id)
            .set(user)
    }

    fun getUser(userId:String):Query{
        return firestore.collection("users").whereEqualTo("id",userId)
    }

    fun uploadFile(imagesStorageReference: StorageReference, stream:FileInputStream): UploadTask {
        return imagesStorageReference.putStream(stream)
    }

    fun uploadFileByUri(imagesStorageReference: StorageReference,file:Uri):UploadTask{
        return imagesStorageReference.putFile(file)
    }


}