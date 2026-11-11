package com.nova.ai.ai.provider

import com.nova.ai.data.preferences.DataStoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiProvider @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : AIProvider {

    override val providerName: String = "Google Gemini"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // গুগলের সক্রিয় আধুনিক মডেল
    private val activeModel = "gemini-2.5-flash"

    override suspend fun generateResponse(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = dataStoreManager.geminiApiKeyFlow.first().trim()
        if (apiKey.isBlank()) {
            return@withContext "অনুগ্রহ করে সেটিংস (Settings) থেকে আপনার Gemini API Key যুক্ত করুন।"
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$activeModel:generateContent?key=$apiKey"

        val systemPrompt = "You are NOVA AI, a smart Android Assistant. Understand Bengali and English seamlessly. Respond in natural Bengali when user asks in Bengali."

        val requestJson = JSONObject().apply {
            val contents = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", "$systemPrompt\n\nUser: $prompt"))
                    })
                })
            }
            put("contents", contents)
        }

        val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            httpClient.newCall(request).execute().use { response ->
                val bodyString = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val errorObj = JSONObject(bodyString).optJSONObject("error")
                    val errorMsg = errorObj?.optString("message") ?: "HTTP ${response.code}"
                    return@withContext "গুগল এআই সংযোগে সমস্যা: $errorMsg"
                }

                val responseJson = JSONObject(bodyString)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val content = candidates.getJSONObject(0).optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text")
                    }
                }
                return@withContext "কোনো উত্তর পাওয়া যায়নি।"
            }
        } catch (e: Exception) {
            return@withContext "সংযোগ ব্যর্থ হয়েছে: ${e.localizedMessage ?: "ইন্টারনেট চেক করুন"}"
        }
    }

    override suspend fun planTaskSteps(userCommand: String): String = withContext(Dispatchers.IO) {
        val apiKey = dataStoreManager.geminiApiKeyFlow.first().trim()
        if (apiKey.isBlank()) return@withContext "{ \"steps\": [] }"

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$activeModel:generateContent?key=$apiKey"

        val taskPrompt = """
            Decompose the following user instruction into a sequential list of steps for Android device execution.
            Instruction: "$userCommand"
            Respond ONLY with a valid JSON object matching:
            {
               "steps": [
                  {"action": "OPEN_APP", "parameter": "Facebook"},
                  {"action": "WAIT", "parameter": "2000"}
               ]
            }
        """.trimIndent()

        val requestJson = JSONObject().apply {
            val contents = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", taskPrompt))
                    })
                })
            }
            put("contents", contents)
        }

        val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            httpClient.newCall(request).execute().use { response ->
                val bodyString = response.body?.string().orEmpty()
                if (!response.isSuccessful) return@withContext "{ \"steps\": [] }"

                val responseJson = JSONObject(bodyString)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val content = candidates.getJSONObject(0).optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text")
                        val cleanJson = text.replace("```json", "").replace("```", "").trim()
                        return@withContext cleanJson
                    }
                }
                "{ \"steps\": [] }"
            }
        } catch (_: Exception) {
            "{ \"steps\": [] }"
        }
    }
}
