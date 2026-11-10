package com.nova.ai.ai.provider

import com.google.ai.client.generativeai.GenerativeModel
import com.nova.ai.data.preferences.DataStoreManager
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiProvider @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : AIProvider {

    override val providerName: String = "Google Gemini"

    private suspend fun getGenerativeModel(): GenerativeModel {
        val apiKey = dataStoreManager.geminiApiKeyFlow.first()
        val keyToUse = apiKey.ifBlank { "GEMINI_FALLBACK_API_KEY_STUB" }

        return GenerativeModel(
            modelName = "gemini-1.5-pro",
            apiKey = keyToUse
        )
    }

    override suspend fun generateResponse(prompt: String): String {
        return try {
            val model = getGenerativeModel()
            val systemContext = """
                You are NOVA AI, an advanced Android Personal Assistant.
                You are helpful, precise, technical, and polite.
                You understand Bengali and English seamlessly. Respond in the language used by the user.
            """.trimIndent()

            val response = model.generateContent("$systemContext\n\nUser: $prompt")
            response.text ?: "I am unable to formulate a response at this moment."
        } catch (e: Exception) {
            "NOVA Brain Communication Error: ${e.localizedMessage ?: "Unknown error"}"
        }
    }

    override suspend fun planTaskSteps(userCommand: String): String {
        return try {
            val model = getGenerativeModel()
            val taskPrompt = """
                Decompose the following user instruction into a sequential list of steps for Android device execution.
                Supported actions:
                - OPEN_APP (parameter: app or package name)
                - CLICK_ELEMENT (parameter: text or button label)
                - WAIT (parameter: milliseconds, e.g. 2000)
                - SCROLL_DOWN (parameter: empty or count)
                - SCROLL_UP (parameter: empty or count)
                - TYPE_TEXT (parameter: text to input)
                
                Instruction: "$userCommand"
                
                Respond ONLY with a valid JSON object matching this structure:
                {
                   "steps": [
                      {"action": "OPEN_APP", "parameter": "Facebook"},
                      {"action": "WAIT", "parameter": "2000"},
                      {"action": "CLICK_ELEMENT", "parameter": "Reels"}
                   ]
                }
            """.trimIndent()

            val response = model.generateContent(taskPrompt)
            response.text ?: "{ \"steps\": [] }"
        } catch (e: Exception) {
            "{ \"steps\": [] }"
        }
    }
}
