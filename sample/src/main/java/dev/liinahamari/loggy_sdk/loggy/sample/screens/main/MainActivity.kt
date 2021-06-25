package dev.liinahamari.loggy_sdk.loggy.sample.screens.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commitNow
import dev.liinahamari.loggy_sdk.loggy.R
import dev.liinahamari.loggy_sdk.helper.FlightRecorder.Companion.i

class MainActivity : AppCompatActivity(R.layout.main_activity) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.commitNow {
                replace(R.id.container, MainFragment.newInstance())
            }
        }

        i { "MainActivity created" }
    }
}