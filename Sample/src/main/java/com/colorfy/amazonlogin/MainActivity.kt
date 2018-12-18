package com.colorfy.amazonlogin

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.othlibs.lwa.AmazonLoginCallback
import com.othlibs.lwa.AmazonLoginHelper
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var amazonLoginHelper: AmazonLoginHelper

    private lateinit var amazonLoginCallback: AmazonLoginCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        amazonLoginHelper = AmazonLoginHelper(this)

        amazonLoginCallback = object : AmazonLoginCallback {
            override fun onSuccess(authorizationCode: String) {
                Log.e(TAG, "authorizationCode: $authorizationCode")
            }

            override fun onError(errorMessage: String?) {
                Log.e(TAG, "errorMessage: $errorMessage")
            }

            override fun onCanceled() {
                Log.e(TAG, "onCanceled")
            }

        }
        button.setOnClickListener { _ ->
            amazonLoginHelper.login("Penguin", "azerudjso", true, true, amazonLoginCallback)
        }
    }

    override fun onResume() {
        super.onResume()
        amazonLoginHelper.onResume()
    }

}
