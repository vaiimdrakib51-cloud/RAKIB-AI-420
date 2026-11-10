package com.nova.ai.domain.tools

data class ToolExecutionResult(
    val success: Boolean,
    val outputMessage: String,
    val errorCode: String? = null
)

interface NovaTool {
    val name: String
    val description: String
    suspend fun execute(parameter: String): ToolExecutionResult
}
