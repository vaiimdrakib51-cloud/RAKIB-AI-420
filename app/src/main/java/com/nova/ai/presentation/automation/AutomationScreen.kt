package com.nova.ai.presentation.automation

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
fun AutomationScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "Workflow Automations",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = NovaPrimaryText
        )
        Text(
            text = "Execute modular multi-action phone sequences",
            style = MaterialTheme.typography.bodyMedium,
            color = NovaSecondaryText
        )

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                PresetAutomationCard(
                    name = "Social Navigation Workflow",
                    stepsCount = 3,
                    description = "Launch app, navigate directly to media feed, observe screen"
                )
            }
            item {
                PresetAutomationCard(
                    name = "System Diagnostic Check",
                    stepsCount = 2,
                    description = "Check internal battery status, OS version, and available storage"
                )
            }
        }
    }
}

@Composable
fun PresetAutomationCard(name: String, stepsCount: Int, description: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NovaCardBackground)
            .border(1.dp, NovaBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = name, style = MaterialTheme.typography.titleMedium, color = NovaPrimaryText)
                Text(text = "$stepsCount steps", style = MaterialTheme.typography.labelSmall, color = NovaAccentCyan)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = description, style = MaterialTheme.typography.bodyMedium, color = NovaSecondaryText)
        }
    }
}
