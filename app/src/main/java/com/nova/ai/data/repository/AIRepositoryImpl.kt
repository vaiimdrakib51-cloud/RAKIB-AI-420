package com.nova.ai.data.repository

import com.nova.ai.ai.provider.GeminiProvider
import com.nova.ai.domain.repository.AIRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIRepositoryImpl @Inject constructor(
    private val geminiProvider: GeminiProvider
) : AIRepository {

    override suspend fun sendPrompt(prompt: String): String {
        return geminiProvider.generateResponse(prompt)
    }

    override suspend fun getDecomposedPlan(command: String): String {
        return geminiProvider.planTaskSteps(command)
    }
}
