package com.servicechowk.app.other

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import java.io.IOException
import java.io.InputStream
import kotlin.text.Charsets.UTF_8

object Utility {

    fun getJson(context: Context): String? {
        var json: String? = null
        json = try {
            // Opening cities.json file
            val `is`: InputStream = context.assets.open("cities.json")
            // is there any content in the file
            val size: Int = `is`.available()
            val buffer = ByteArray(size)
            // read values in the byte array
            `is`.read(buffer)
            // close the stream --- very important
            `is`.close()
            // convert byte to string
            String(buffer,UTF_8)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return json
        }
        return json
    }

    fun enableDarkTheme(b: Boolean){
        if (b)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }


}