package com.nova.ai.domain.tools

import android.content.Context
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DeviceInfoTool @Inject constructor(
    @ApplicationContext private val context: Context
) : NovaTool {

    override val name: String = "DeviceInfo"
    override val description: String = "Reports battery, storage, and OS level diagnostic data"

    override suspend fun execute(parameter: String): ToolExecutionResult {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryPct = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

        val stat = StatFs(Environment.getDataDirectory().path)
        val bytesAvailable = stat.availableBlocksLong * stat.blockSizeLong
        val gigabytesAvailable = bytesAvailable / (1024 * 1024 * 1024)

        val report = """
            Device: ${Build.MANUFACTURER} ${Build.MODEL}
            Android Version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
            Battery: $batteryPct%
            Internal Storage Free: $gigabytesAvailable GB
        """.trimIndent()

        return ToolExecutionResult(
            success = true,
            outputMessage = report
        )
    }
}
