package com.brianhenning.cribbage

import android.app.Application
import android.util.Log
import com.perimeterx.mobile_sdk.PerimeterX
import com.perimeterx.mobile_sdk.PerimeterXDelegate
import com.perimeterx.mobile_sdk.main.PXPolicy
import com.perimeterx.mobile_sdk.main.PXPolicyUrlRequestInterceptionType
import com.perimeterx.mobile_sdk.main.PXStorageMethod

class CribbageApplication : Application(), PerimeterXDelegate {
    override fun onCreate() {
        super.onCreate()
        val policy = PXPolicy()
        val appId = "PXjJ0cYtn9"
        policy.setDomains(arrayListOf("www.brianhenning.com"), appId)
        policy.storageMethod = PXStorageMethod.DATA_STORE
        policy.urlRequestInterceptionType = PXPolicyUrlRequestInterceptionType.INTERCEPT_AND_RETRY_REQUEST
        //policy.doctorCheckEnabled = true

        println("SDK version: ${PerimeterX.sdkVersion()}")
        Log.i("CribbageApplication", "SDK version: ${PerimeterX.sdkVersion()}")
        try {
            PerimeterX.start(this, appId, this, policy)
        }
        catch (exception: Exception) {
            Log.i("CribbageApplication", "failed to start. error: ${exception.message}")
        }
    }

    override fun perimeterxChallengeCancelledHandler(appId: String) {
        Log.i("CribbageApplication", "ChallengeCancelledHandler")
    }

    override fun perimeterxChallengeSolvedHandler(appId: String) {
        Log.i("CribbageApplication", "ChallengeSolvedHandler")
    }

    override fun perimeterxHeadersWereUpdated(headers: HashMap<String, String>, appId: String) {
        Log.i("CribbageApplication", "HeadersWereUpdated")
    }

    override fun perimeterxRequestBlockedHandler(url: String?, appId: String) {
        Log.i("CribbageApplication", "RequestBlockedHandler")
    }
    
    override fun perimeterxChallengeRenderFailedHandler(appId: String) {
        Log.i("CribbageApplication", "ChallengeRenderFailedHandler")
    }
    
    override fun perimeterxChallengeRenderedHandler(appId: String) {
        Log.i("CribbageApplication", "ChallengeRenderedHandler")
    }
}