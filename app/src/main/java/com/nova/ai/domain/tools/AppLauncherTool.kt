package com.nova.ai.domain.tools

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AppLauncherTool @Inject constructor(
    @ApplicationContext private val context: Context
) : NovaTool {

    override val name: String = "AppLauncher"
    override val description: String = "Launches an installed Android application by name"

    override suspend fun execute(parameter: String): ToolExecutionResult {
        val pm: PackageManager = context.packageManager
        val query = parameter.trim().lowercase()

        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        var targetPackage: String? = null

        for (app in installedApps) {
            val label = pm.getApplicationLabel(app).toString().lowercase()
            if (label == query || app.packageName.lowercase().contains(query)) {
                targetPackage = app.packageName
                break
            }
        }

        if (targetPackage == null) {
            return ToolExecutionResult(
                success = false,
                outputMessage = "Application '$parameter' was not found on this device."
            )
        }

        val launchIntent = pm.getLaunchIntentForPackage(targetPackage)
        return if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            ToolExecutionResult(
                success = true,
                outputMessage = "Successfully launched $parameter ($targetPackage)."
            )
        } else {
            ToolExecutionResult(
                success = false,
                outputMessage = "Failed to launch $parameter: intent was unavailable."
            )
        }
    }
}
