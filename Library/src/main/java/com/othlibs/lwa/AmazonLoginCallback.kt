package com.othlibs.lwa

interface AmazonLoginCallback {

    fun onSuccess(authorizationCode: String)

    fun onError(errorMessage: String?)

    fun onCanceled()
}