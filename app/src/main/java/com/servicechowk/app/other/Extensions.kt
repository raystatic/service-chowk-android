package com.servicechowk.app.other

import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

object Extensions {

    fun Context.showToast(message:String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun View.showSnack(message: String){
        Snackbar.make(this,message,Snackbar.LENGTH_SHORT).show()
    }

    fun EditText.validateIsNotError(errorMessage:String):Boolean{
        val input = this.text.toString()
        if (input.isEmpty()){
            this.setError(errorMessage)
            return false
        }
        this.setError(null)
        return true
    }

}