package com.example.umbrella

import android.Manifest
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

fun Log(msg: String) {
    Log.d("hayato", msg)
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class MainActivity : AppCompatActivity() {

    fun setupBackButton(enableBackButton: Boolean) {
        val actionBar: ActionBar? = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(enableBackButton)
    }

    private fun addFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment).commit()
    }

    private val alignment = TwoPointAlignment

    private fun toAlignmentType() {
        val fragment = AlignmentTypeFragment()
        fragment.onNextClicked = {
            toAlignmentAlgorithm()
        }

        addFragment(fragment)
    }

    private fun toAlignmentAlgorithm() {
        val fragment = AlignmentAlgorithmFragment()
        fragment.onNextClicked = {
            toAlignmentOrigin()
        }

        addFragment(fragment)
    }

    private fun toAlignmentOrigin() {
        val fragment = AlignmentOriginFragment()
        fragment.onNextClicked = {
            toAlignmentModelSize()
        }

        addFragment(fragment)
    }

    private fun toAlignmentModelSize() {
        val fragment = AlignmentModelSizeFragment()
        addFragment(fragment)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            ), 1
        )

        alignment.image = BitmapFactory.decodeResource(resources, R.drawable.sample_image)

        addFragment(HomeFragment())
        //addFragment(LiveFragment())
        //toAlignmentOrigin()
    }
}
