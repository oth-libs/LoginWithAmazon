package com.othlibs.lwa

interface AmazonLoginCallback {

    fun onSuccess(response: String, codeVerifier: String, codeVerifierHashed: String)

    fun onError(errorMessage: String?)

    fun onCanceled()
}