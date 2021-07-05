package com.servicechowk.app.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.servicechowk.app.other.Utility

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Utility.enableDarkTheme(false)

    }
}