package com.example

import android.app.Application
import android.util.Log
import go.Seq
import libv2ray.Libv2ray

class VRayPulseApp : Application() {

    override fun onCreate() {
        super.onCreate()
        try {
            Seq.setContext(this)
            Libv2ray.initCoreEnv(filesDir.absolutePath, "")
            com.example.optimization.OptimizationManager.init(this)
            Log.i("VRayPulseApp", "VRayPulseApp initialized with Xray runtime and Optimization Manager")
        } catch (e: Exception) {
            Log.e("VRayPulseApp", "Failed to initialize Xray runtime context", e)
        }
    }
}
