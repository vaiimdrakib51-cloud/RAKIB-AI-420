package com.nova.ai.presentation.assistant

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nova.ai.automation.planner.TaskPlanner
import com.nova.ai.device.overlay.FloatingAssistantService
import com.nova.ai.domain.model.ChatMessage
import com.nova.ai.domain.model.MessageSender
import com.nova.ai.domain.repository.AIRepository
import com.nova.ai.voice.speechinput.VoiceRecognitionManager
import com.nova.ai.voice.speechoutput.TextToSpeechManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AssistantUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isListening: Boolean = false,
    val isThinking: Boolean = false,
    val liveTaskTitle: String = "NOVA Core Standby",
    val connectionStatus: String = "Online",
    val isFloatingServiceActive: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AssistantViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val aiRepository: AIRepository,
    private val taskPlanner: TaskPlanner,
    private val voiceRecognitionManager: VoiceRecognitionManager,
    private val ttsManager: TextToSpeechManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    init {
        initInitialGreeting()
        observeVoiceInput()
    }

    private fun initInitialGreeting() {
        val initialMessage = ChatMessage(
            text = "Welcome to NOVA AI. Speak or enter a command in English or Bengali.",
            sender = MessageSender.ASSISTANT
        )
        _uiState.update { it.copy(messages = listOf(initialMessage)) }
    }

    private fun observeVoiceInput() {
        viewModelScope.launch {
            voiceRecognitionManager.speechResultFlow.collect { recognizedSpeech ->
                if (recognizedSpeech.isNotBlank()) {
                    handleUserCommand(recognizedSpeech)
                }
            }
        }
        viewModelScope.launch {
            voiceRecognitionManager.isListeningFlow.collect { listening ->
                _uiState.update { it.copy(isListening = listening) }
            }
        }
    }

    fun handleUserCommand(rawCommand: String) {
        if (rawCommand.isBlank() || _uiState.value.isThinking) return

        val userMessage = ChatMessage(text = rawCommand, sender = MessageSender.USER)
        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                isThinking = true,
                liveTaskTitle = "Analyzing command..."
            )
        }

        viewModelScope.launch {
            try {
                val plan = taskPlanner.decompose(rawCommand)
                if (plan.steps.isNotEmpty()) {
                    _uiState.update { it.copy(liveTaskTitle = "Executing plan (${plan.steps.size} steps)") }
                    taskPlanner.executePlan(plan)
                    val responseMessage = ChatMessage(
                        text = "Understood. Executing ${plan.steps.size} planned automation tasks.",
                        sender = MessageSender.ASSISTANT
                    )
                    _uiState.update { it.copy(messages = it.messages + responseMessage, isThinking = false) }
                    ttsManager.speak(responseMessage.text)
                } else {
                    val response = aiRepository.sendPrompt(rawCommand)
                    val assistantMessage = ChatMessage(text = response, sender = MessageSender.ASSISTANT)
                    _uiState.update {
                        it.copy(
                            messages = it.messages + assistantMessage,
                            isThinking = false,
                            liveTaskTitle = "Standby"
                        )
                    }
                    ttsManager.speak(assistantMessage.text)
                }
            } catch (e: Exception) {
                val errorMsg = "Error processing request: ${e.localizedMessage ?: "Unknown error"}"
                _uiState.update {
                    it.copy(
                        messages = it.messages + ChatMessage(text = errorMsg, sender = MessageSender.ASSISTANT),
                        isThinking = false,
                        liveTaskTitle = "Execution Halted"
                    )
                }
                ttsManager.speak("An error occurred while executing that command.")
            }
        }
    }

    fun toggleVoiceListening() {
        if (_uiState.value.isListening) {
            voiceRecognitionManager.stopListening()
        } else {
            voiceRecognitionManager.startListening()
        }
    }

    fun toggleFloatingOverlay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            _uiState.update { it.copy(errorMessage = "Grant Overlay Permission in Settings") }
            return
        }

        val intent = Intent(context, FloatingAssistantService::class.java)
        if (_uiState.value.isFloatingServiceActive) {
            context.stopService(intent)
            _uiState.update { it.copy(isFloatingServiceActive = false) }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            _uiState.update { it.copy(isFloatingServiceActive = true) }
        }
    }

    fun stopCurrentExecution() {
        taskPlanner.abortCurrentPlan()
        voiceRecognitionManager.stopListening()
        ttsManager.stop()
        _uiState.update {
            it.copy(
                isThinking = false,
                isListening = false,
                liveTaskTitle = "Stopped by user"
            )
        }
    }
}
