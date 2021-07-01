package com.servicechowk.app.other

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import pub.devrel.easypermissions.EasyPermissions
import java.io.IOException
import java.io.InputStream
import kotlin.text.Charsets.UTF_8

object Utility {

    private fun savePhotoToExternalStorage(activity: AppCompatActivity, dislayName:String, bmp: Bitmap):String?{
        val imageCollection = sdk29AndUp {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME,"$dislayName.jpg")
            put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg")
            put(MediaStore.Images.Media.WIDTH,bmp.width)
            put(MediaStore.Images.Media.HEIGHT,bmp.height)
        }

        return try {
            var outputUri :String?=""
            activity.contentResolver.insert(imageCollection,contentValues)?.also {uri ->
                outputUri = uri.path
                activity.contentResolver.openOutputStream(uri).use {outputStream ->
                    if (!bmp.compress(Bitmap.CompressFormat.JPEG,100,outputStream)){
                        throw IOException("Couldn't save bitmap")
                    }
                }
            } ?: throw IOException("Couldn't create MediaStore entry")
            outputUri
        }catch (e:IOException){
            e.printStackTrace()
            null
        }

    }


    fun hasStoragePermissions(context: Context) =

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }else{
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

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