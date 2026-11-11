package com.nova.ai.presentation.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nova.ai.presentation.theme.*

@Composable
fun ActivityScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "Live Activity Monitor",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = NovaPrimaryText
        )
        Text(
            text = "Real-time verification of on-device automation steps",
            style = MaterialTheme.typography.bodyMedium,
            color = NovaSecondaryText
        )

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ActivityStatusCard(
                    title = "App Launch Engine",
                    description = "Verifies background and foreground process dispatch",
                    status = "READY",
                    isSuccess = true
                )
            }
            item {
                ActivityStatusCard(
                    title = "Accessibility Tree Observer",
                    description = "Inspects visible UI element hierarchies for execution",
                    status = "CONNECTED",
                    isSuccess = true
                )
            }
            item {
                ActivityStatusCard(
                    title = "Continuous Voice Pipeline",
                    description = "Foreground listening channel status",
                    status = "STANDBY",
                    isSuccess = true
                )
            }
        }
    }
}

@Composable
fun ActivityStatusCard(
    title: String,
    description: String,
    status: String,
    isSuccess: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NovaCardBackground)
            .border(1.dp, NovaBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, color = NovaPrimaryText)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = description, style = MaterialTheme.typography.bodyMedium, color = NovaSecondaryText)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSuccess) NovaSuccess.copy(alpha = 0.15f) else NovaError.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = status,
                    color = if (isSuccess) NovaSuccess else NovaError,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
