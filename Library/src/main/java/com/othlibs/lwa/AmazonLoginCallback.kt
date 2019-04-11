package com.othlibs.lwa

interface AmazonLoginCallback {

    fun onSuccess(response: String)

    fun onError(errorMessage: String?)

    fun onCanceled()
}