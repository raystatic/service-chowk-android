package com.servicechowk.app.other

import android.content.Context
import android.content.SharedPreferences

const val PRIVATEMODE = 0
class PrefManager(context: Context) {

    private var pref: SharedPreferences?=null

    init {
        pref = context.getSharedPreferences(
                Constants.KEY_PREF_NAME,
                PRIVATEMODE
        )

    }

    fun saveString(key: String?, data: String?) {
        pref?.edit()?.apply {
            putString(key, data)
        }?.apply()
    }

    fun saveInteger(key: String?, data: Int) {
        pref?.edit()?.apply {
            putInt(key, data)
        }?.apply()
    }

    fun saveBoolean(key: String?, `val`: Boolean?) {
        pref?.edit()?.apply {
            putBoolean(key, `val`?:false)
        }?.apply()
    }

    fun getBoolean(key: String?): Boolean? {
        return pref?.getBoolean(key, false)
    }

    fun getString(key: String?): String? {
        return pref?.getString(key, null)
    }

    fun getInt(key: String?): Int? {
        return pref?.getInt(key, 0)
    }

    fun saveSetString(key:String?,`val`:HashSet<String>){
        pref?.edit()?.apply {
            putStringSet(key,`val`)
        }?.apply()
    }

    fun getSetString(key:String):Set<String>?{
        return pref?.getStringSet(key,null)
    }


}