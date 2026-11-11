package com.nova.ai.presentation.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nova.ai.presentation.theme.*

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val geminiKey by viewModel.geminiKey.collectAsState()
    var inputKey by remember(geminiKey) { mutableStateOf(geminiKey) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "Settings & Security",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = NovaPrimaryText
        )
        Text(
            text = "Configure AI intelligence credentials and platform privileges",
            style = MaterialTheme.typography.bodyMedium,
            color = NovaSecondaryText
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Gemini API Credentials",
            style = MaterialTheme.typography.titleMedium,
            color = NovaPrimaryText
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(NovaCardBackground)
                .border(1.dp, NovaBorder, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            BasicTextField(
                value = inputKey,
                onValueChange = {
                    inputKey = it
                    viewModel.saveGeminiKey(it)
                },
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = NovaPrimaryText),
                cursorBrush = SolidColor(NovaAccentCyan),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "System Privileges",
            style = MaterialTheme.typography.titleMedium,
            color = NovaPrimaryText
        )
        Spacer(modifier = Modifier.height(8.dp))

        SettingActionRow(
            title = "Accessibility Engine",
            subtitle = "Required to navigate apps and perform clicks"
        ) {
            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        Spacer(modifier = Modifier.height(10.dp))

        SettingActionRow(
            title = "Overlay Permission",
            subtitle = "Required to display the floating assistant bubble"
        ) {
            context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
        }
    }
}

@Composable
fun SettingActionRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NovaCardBackground)
            .border(1.dp, NovaBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = NovaPrimaryText)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = NovaSecondaryText)
        }
    }
}
