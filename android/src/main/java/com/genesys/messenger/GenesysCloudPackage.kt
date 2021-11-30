package com.genesys.messenger

import android.view.View
import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ReactShadowNode
import com.facebook.react.uimanager.ViewManager
import java.util.Collections

class GenesysCloudPackage : ReactPackage {

    override fun createViewManagers(reactContext: ReactApplicationContext): MutableList<ViewManager<out View, out ReactShadowNode<*>>> {
        return Collections.emptyList()
    }

   override fun createNativeModules (reactContext:ReactApplicationContext ) : MutableList<NativeModule> {
       return mutableListOf<NativeModule>(GenesysCloud(reactContext));
   }

}