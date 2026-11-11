package com.nova.ai.presentation.assistant

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.nova.ai.R
import com.nova.ai.domain.model.ChatMessage
import com.nova.ai.domain.model.MessageSender
import com.nova.ai.presentation.theme.*

// অ্যাক্সেসিবিলিটি সার্ভিস অন আছে কিনা চেক করার ফাংশন
fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
    return enabledServices.any { it.resolveInfo.serviceInfo.packageName == context.packageName }
}

@Composable
fun AssistantScreen(
    viewModel: AssistantViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // পারমিশন স্ট্যাটাস ট্র্যাক করার স্টেট
    var isMicGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }
    var isAccessibilityGranted by remember {
        mutableStateOf(isAccessibilityServiceEnabled(context))
    }
    var isOverlayGranted by remember {
        mutableStateOf(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(context) else true)
    }

    // অ্যাপে ফিরে আসলে স্বয়ংক্রিয়ভাবে পারমিশন চেক করা
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isMicGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                isAccessibilityGranted = isAccessibilityServiceEnabled(context)
                isOverlayGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(context) else true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // কোনো পারমিশন বাকি থাকলে পপ-আপ দেখাবে
    val showPermissionModal = !isMicGranted || !isAccessibilityGranted || !isOverlayGranted

    val recordAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        isMicGranted = granted
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // === পারমিশন পপ-আপ মডাল ===
    if (showPermissionModal) {
        Dialog(onDismissRequest = { /* বাইরে চাপলে কাটবে না, পারমিশন আবশ্যক */ }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(NovaCardBackground)
                    .border(1.dp, NovaAccentCyan, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_nova_logo),
                        contentDescription = "Logo",
                        tint = NovaAccentCyan,
                        modifier = Modifier.size(42.dp)
                    )

                    Text(
                        text = "প্রয়োজনীয় পারমিশন সেটআপ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NovaPrimaryText
                    )

                    Text(
                        text = "NOVA AI দিয়ে আপনার ফোন কন্ট্রোল এবং অ্যাপ ওপেন করাতে নিচের পারমিশনগুলো চালু করুন:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NovaSecondaryText
                    )

                    // ১. অ্যাক্সেসিবিলিটি সার্ভিস বাটন
                    PermissionItemRow(
                        title = "১. ফোন কন্ট্রোল (Accessibility)",
                        isGranted = isAccessibilityGranted,
                        onClick = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        }
                    )

                    // ২. ডিসপ্লে ওভারলে বাটন
                    PermissionItemRow(
                        title = "২. স্ক্রিনের ওপর ডিসপ্লে (Overlay)",
                        isGranted = isOverlayGranted,
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                                context.startActivity(intent)
                            }
                        }
                    )

                    // ৩. মাইক্রোফোন বাটন
                    PermissionItemRow(
                        title = "৩. মুখের কথা শোনা (Microphone)",
                        isGranted = isMicGranted,
                        onClick = {
                            recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    )
                }
            }
        }
    }

    // === প্রধান ড্যাশবোর্ড UI ===
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaBackground)
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_nova_logo),
                    contentDescription = "Logo",
                    tint = NovaAccentCyan,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "NOVA AI",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = NovaPrimaryText
                    )
                    Text(
                        text = "Status: ${uiState.connectionStatus}",
                        style = MaterialTheme.typography.labelSmall,
                        color = NovaSuccess
                    )
                }
            }

            IconButton(
                onClick = { viewModel.toggleFloatingOverlay() },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (uiState.isFloatingServiceActive) NovaAccentCyan.copy(alpha = 0.2f) else NovaCardBackground)
                    .border(1.dp, NovaBorder, CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_screen_control),
                    contentDescription = "Floating Overlay Toggle",
                    tint = if (uiState.isFloatingServiceActive) NovaAccentCyan else NovaSecondaryText,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(NovaCardBackground)
                .border(1.dp, NovaBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_waveform),
                        contentDescription = "Waveform",
                        tint = NovaAccentCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uiState.liveTaskTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = NovaPrimaryText
                    )
                }

                if (uiState.isThinking) {
                    IconButton(
                        onClick = { viewModel.stopCurrentExecution() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_stop),
                            contentDescription = "Stop execution",
                            tint = NovaError,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(uiState.messages) { msg ->
                ChatBubble(msg)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(NovaCardBackground)
                    .border(1.dp, NovaBorder, RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (inputText.isEmpty()) {
                    Text(
                        text = "Speak or enter command...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NovaMutedText
                    )
                }
                BasicTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = NovaPrimaryText),
                    cursorBrush = SolidColor(NovaAccentCyan),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (isMicGranted) {
                        viewModel.toggleVoiceListening()
                    } else {
                        recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (uiState.isListening) NovaError else NovaPrimaryPurple)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_mic),
                    contentDescription = "Microphone",
                    tint = NovaPrimaryText,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.handleUserCommand(inputText)
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(NovaSecondaryBlue)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_send),
                    contentDescription = "Send",
                    tint = NovaPrimaryText,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun PermissionItemRow(
    title: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !isGranted,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isGranted) NovaSuccess.copy(alpha = 0.2f) else NovaSecondaryBlue,
            disabledContainerColor = NovaSuccess.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = if (isGranted) NovaSuccess else NovaPrimaryText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (isGranted) "অন করা আছে ✓" else "অন করুন →",
                color = if (isGranted) NovaSuccess else NovaAccentCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.sender == MessageSender.USER
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bubbleColor = if (isUser) NovaPrimaryPurple.copy(alpha = 0.35f) else NovaCardBackground
    val borderCol = if (isUser) NovaPrimaryPurple else NovaBorder

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 16.dp
                    )
                )
                .background(bubbleColor)
                .border(
                    width = 1.dp,
                    color = borderCol,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 16.dp
                    )
                )
                .padding(14.dp)
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyLarge,
                color = NovaPrimaryText
            )
        }
    }
}
