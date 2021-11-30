package com.genesys.messenger

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod


class GenesysCloud(context: ReactApplicationContext) : ReactContextBaseJavaModule(context) {
    override fun getName(): String {
        return "GenesysCloud"
    }

    @ReactMethod
    fun startChat(
        deploymentId: String, domain: String, tokenStoreKey: String, logging: Boolean
    ) {
        reactApplicationContext.let {
            currentActivity?.run {
                startActivity(
                    GenesysCloudChatActivity.intentFactory(
                        deploymentId, domain, tokenStoreKey, logging
                    )
                );
            }
        }
    }
}
