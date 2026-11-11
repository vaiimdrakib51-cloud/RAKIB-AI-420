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

    // গুগলের সক্রিয় আধুনিক মডেলগুলোর তালিকা (একটি না পেলে স্বয়ংক্রিয়ভাবে অন্যটি চলবে)
    private val candidateModels = listOf(
        "gemini-2.5-flash",
        "gemini-2.0-flash",
        "gemini-2.0-flash-exp",
        "gemini-1.5-flash-latest"
    )

    override suspend fun generateResponse(prompt: String): String {
        val apiKey = dataStoreManager.geminiApiKeyFlow.first()
        val keyToUse = apiKey.trim()
        if (keyToUse.isBlank()) {
            return "অনুগ্রহ করে সেটিংস (Settings) থেকে আপনার সঠিক Gemini API Key যুক্ত করুন।"
        }

        val systemContext = """
            You are NOVA AI, an advanced Android Personal Assistant.
            You are helpful, precise, technical, and polite.
            Always understand Bengali and English seamlessly. Respond in natural, polite Bengali when the user speaks or writes in Bengali.
        """.trimIndent()

        var lastErrorMessage = ""

        // স্বয়ংক্রিয়ভাবে সক্রিয় মডেল খুঁজে বের করে উত্তর তৈরি করা
        for (modelName in candidateModels) {
            try {
                val model = GenerativeModel(
                    modelName = modelName,
                    apiKey = keyToUse
                )
                val response = model.generateContent("$systemContext\n\nUser: $prompt")
                val text = response.text
                if (!text.isNullOrBlank()) {
                    return text
                }
            } catch (e: Exception) {
                lastErrorMessage = e.localizedMessage ?: e.message ?: "Unknown error"
                // এই মডেলে সমস্যা হলে লুপের পরবর্তী মডেলে চেষ্টা করবে
            }
        }

        return "NOVA Brain Communication Error: $lastErrorMessage"
    }

    override suspend fun planTaskSteps(userCommand: String): String {
        val apiKey = dataStoreManager.geminiApiKeyFlow.first()
        val keyToUse = apiKey.trim()
        if (keyToUse.isBlank()) return "{ \"steps\": [] }"

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

        for (modelName in candidateModels) {
            try {
                val model = GenerativeModel(
                    modelName = modelName,
                    apiKey = keyToUse
                )
                val response = model.generateContent(taskPrompt)
                val text = response.text
                if (!text.isNullOrBlank()) {
                    return text
                }
            } catch (_: Exception) {
                // ব্যর্থ হলে পরবর্তী মডেলে চেষ্টা করবে
            }
        }

        return "{ \"steps\": [] }"
    }
}
