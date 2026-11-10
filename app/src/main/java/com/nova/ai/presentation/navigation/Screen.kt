package com.nova.ai.presentation.navigation

import androidx.annotation.DrawableRes
import com.nova.ai.R

sealed class Screen(val route: String, val title: String, @DrawableRes val iconRes: Int) {
    data object Assistant : Screen("assistant_screen", "Assistant", R.drawable.ic_nav_assistant)
    data object Automation : Screen("automation_screen", "Automations", R.drawable.ic_nav_automation)
    data object Activity : Screen("activity_screen", "Activity", R.drawable.ic_nav_activity)
    data object Settings : Screen("settings_screen", "Settings", R.drawable.ic_nav_settings)

    companion object {
        val bottomNavItems = listOf(Assistant, Automation, Activity, Settings)
    }
}
