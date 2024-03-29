package com.servicechowk.app.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    val id:String,
    var name:String? = "",
    var workField:String?="",
    var city:String?="",
    var locality:String?="",
    var contactNumber:String?="",
    var experience:String?="",
    var equipmentAvailable:Boolean?=false,
    var education:String?="",
    var lastWorkAt:String?="",
    var photoId:String?="",
    var photo:String?="",
    var workPhoto:String?="",
    var isVerified:Boolean?=false,
    var fcmToken:String?=""
):Parcelable{
    constructor():this("")
}