package com.othlibs.lwa

import android.app.Activity
import android.util.Log
import com.amazon.identity.auth.device.AuthError
import com.amazon.identity.auth.device.api.authorization.*
import com.amazon.identity.auth.device.api.workflow.RequestContext
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject

class AmazonLoginHelper(private val activity: Activity) {

    companion object {
        private val TAG = "AmazonLoginHelper"
        val LIBRARY_VERSION = BuildConfig.VERSION_NAME
    }


    private var requestContext: RequestContext = RequestContext.create(activity.applicationContext)
    private val codeVerifier: String = CodeChallengeGenerator.getInstance().codeVerifier
    private lateinit var codeVerifierHashed: String
    private var amazonLoginCallback: AmazonLoginCallback? = null

    /**
     * Used to manually check on "onCancel" event, in case user pressed hw back (not handled by the library)
     */
    private var loginTriggered = false

    init {
        requestContext.registerListener(object : AuthorizeListener() {

            /* Authorization was completed successfully. */
            override fun onSuccess(authorizeResult: AuthorizeResult) {
                loginTriggered = false


                activity.runOnUiThread {
                    Log.d(TAG, authorizeResult.redirectURI)
                    Log.d(TAG, authorizeResult.authorizationCode)
                    Log.d(TAG, authorizeResult.clientId)

                    Log.e(TAG, "codeVerifierHashed: $codeVerifierHashed")

                    amazonLoginCallback?.onSuccess(Gson().toJson(authorizeResult), codeVerifierHashed)
                }
            }

            /* There was an error during the attempt to authorize the application */
            override fun onError(authError: AuthError) {
                loginTriggered = false

                activity.runOnUiThread {
                    Log.e(TAG, "Error during authorization. Please try again.")
                    Log.e(TAG, "AuthError during authorization", authError)
                    authError.printStackTrace()

                    amazonLoginCallback?.onError(authError.message)
                }
            }

            /* Authorization was cancelled before it could be completed. */
            override fun onCancel(authCancellation: AuthCancellation) {
                loginTriggered = false

                activity.runOnUiThread {
                    Log.i(TAG, "Authorization cancelled.")
                    amazonLoginCallback?.onCanceled()
                }
            }
        })
    }

    fun login(deviceModel: String, serial: String, testDevice: Boolean, includeNonLive: Boolean, amazonLoginCallback: AmazonLoginCallback) {
        this.amazonLoginCallback = amazonLoginCallback
        this.loginTriggered = true

        getScope(deviceModel, serial, testDevice, includeNonLive)?.let {


            codeVerifierHashed = CodeChallengeGenerator.getInstance().generateCodeChallenge(codeVerifier, CodeChallengeGenerator.SHA_256)
            Log.i(TAG, "codeVerifierHashed: $codeVerifierHashed")

            AuthorizationManager.authorize(
                    AuthorizeRequest.Builder(requestContext)
                            .addScopes(it)
                            .forGrantType(AuthorizeRequest.GrantType.AUTHORIZATION_CODE)
                            .withProofKeyParameters(codeVerifierHashed, "S256")
                            .build()
            )
        }
    }


    private fun getScope(deviceModel: String, serial: String, testDevice: Boolean, includeNonLive: Boolean): Scope? {
        //YOUR_DEVICE_MODEL_NAME - The model ID of your device obtained from self-service portal.
        //YOUR_DEVICE_SERIAL_NUMBER – The serial number of the device you are associating with the DRS service. Only alphanumeric characters can be used [A-Za-z0-9], for a maximum string length of 50 characters.
        //IS_THIS_A_TEST_DEVICE – Flag that indicates if this a test device or not. You will not be able to test devices without setting the `is_test_device` flag to true, but you must set it to false in production. Test devices will not place real orders
        // SHOULD_INCLUDE_NON_LIVE if true, allows the registration to proceed using device capabilities that have not yet been certified by Amazon. You can use this parameter to test your system while awaiting Amazon certification.
        val scopeDataString = "{\"device_model\":\"$deviceModel\", \"serial\":\"$serial\", \"is_test_device\":\"$testDevice\", \"should_include_non_live\":\"$includeNonLive\"}"

        try {
            return ScopeFactory.scopeNamed("dash:replenish", JSONObject(scopeDataString))
        } catch (e: JSONException) {
            Log.e(TAG, "Error during scope data JSON object creation", e)
        }

        return null
    }


    fun onResume() {
        Log.e(TAG, "onResume")

        requestContext.onResume()

        if (loginTriggered) {
            loginTriggered = false

            amazonLoginCallback?.onCanceled()
        }
    }
}
