package com.example.util

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build

data class DeviceInfo(
    val modelName: String,
    val brand: String,
    val batteryPercent: Int
) {
    val displayBadgeText: String
        get() = "$modelName  ${brand.uppercase()} · $batteryPercent%"
}

object DeviceInfoHelper {

    fun getDeviceInfo(context: Context): DeviceInfo {
        val model = Build.MODEL ?: "Android Device"
        val brand = Build.BRAND ?: "Device"

        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            context.registerReceiver(null, filter)
        }

        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1

        val batteryPct = if (level >= 0 && scale > 0) {
            (level * 100 / scale.toFloat()).toInt()
        } else {
            100
        }

        return DeviceInfo(
            modelName = model,
            brand = brand,
            batteryPercent = batteryPct
        )
    }
}
