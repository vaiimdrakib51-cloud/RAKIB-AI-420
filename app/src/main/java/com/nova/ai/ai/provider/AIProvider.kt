package com.nova.ai.ai.provider

interface AIProvider {
    val providerName: String
    suspend fun generateResponse(prompt: String): String
    suspend fun planTaskSteps(userCommand: String): String
}
