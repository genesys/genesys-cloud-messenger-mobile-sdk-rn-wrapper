package com.genesys.messenger

import android.content.Intent
import android.content.pm.ActivityInfo
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.genesys.cloud.core.utils.NRError


internal fun ReactContext?.emitError(error: NRError) {
    this?:return

    val event = Arguments.createMap().apply {
        putString("errorCode", error.errorCode)
        putString("reason", error.reason)
        putString("message", error.description)
    }

    getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
        ?.emit("onMessengerError", event)
}

internal fun ReactContext?.emitState(state: String) {
    this?:return

    val event = Arguments.createMap().apply {
        putString("state", state)
    }

    getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
        ?.emit("onMessengerState", event)
}

class GenesysCloud(context: ReactApplicationContext) : ReactContextBaseJavaModule(context) {

    private var screenOrientation: Int = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

    override fun getName(): String {
        return "GenesysCloud"
    }

    @ReactMethod
    fun startChat(deploymentId: String, domain: String, tokenStoreKey: String, logging: Boolean) {
        reactApplicationContext.let {
            currentActivity?.run {
                startActivity(GenesysCloudChatActivity.intentFactory(deploymentId, domain, tokenStoreKey, logging,
                    screenOrientation));
            }
        }
    }

    /**
     * Request screen orientation
     * > Values < -1 or >= 14 are handled as ActivityInfo.SCREEN_ORIENTATION_LOCKED
     *
     * @param orientation
     */
    @ReactMethod
    fun requestScreenOrientation(orientation: Int) {
        screenOrientation = orientation
    }

    override fun getConstants(): Map<String, Any>? {
        val constants: MutableMap<String, Any> = HashMap()
        constants["SCREEN_ORIENTATION_PORTRAIT"] = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        constants["SCREEN_ORIENTATION_LANDSCAPE"] = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        constants["SCREEN_ORIENTATION_UNSPECIFIED"] = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        constants["SCREEN_ORIENTATION_LOCKED"] = ActivityInfo.SCREEN_ORIENTATION_LOCKED

        constants["ConfigurationsError"] = NRError.ConfigurationsError
        constants["ForbiddenError"] = NRError.ForbiddenError

        return constants
    }

}
