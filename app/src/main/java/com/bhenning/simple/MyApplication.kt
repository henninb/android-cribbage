package com.bhenning.simple

import android.app.Application
import com.perimeterx.mobile_sdk.PerimeterX
import com.perimeterx.mobile_sdk.PerimeterXDelegate
import com.perimeterx.mobile_sdk.main.PXPolicy

class MyApplication : Application(), PerimeterXDelegate {
    override fun onCreate() {
        super.onCreate()
        val policy = PXPolicy()
        val appId = "test"
        policy.setDomains(arrayListOf("example.com"), appId)

        try {
            PerimeterX.start(this, appId, this, policy)
        }
        catch (exception: Exception) {
            println("failed to start. error: ${exception.message}")
        }

        try {
            PerimeterX.setCustomParameters(hashMapOf("custom_param1" to "hello", "custom_param2" to "world"))
        }
        catch (exception: Exception) {
            println("error: ${exception.message}")
        }
    }

    override fun perimeterxChallengeCancelledHandler(appId: String) {
    }

    override fun perimeterxChallengeSolvedHandler(appId: String) {
    }

    override fun perimeterxHeadersWereUpdated(headers: HashMap<String, String>, appId: String) {
    }

    override fun perimeterxRequestBlockedHandler(url: String?, appId: String) {
    }
}