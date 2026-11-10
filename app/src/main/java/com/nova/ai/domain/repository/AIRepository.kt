package com.nova.ai.domain.repository

interface AIRepository {
    suspend fun sendPrompt(prompt: String): String
    suspend fun getDecomposedPlan(command: String): String
}
