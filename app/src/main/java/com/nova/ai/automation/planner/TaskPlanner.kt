package com.nova.ai.automation.planner

import com.nova.ai.device.accessibility.AccessibilityAutomationService
import com.nova.ai.domain.repository.AIRepository
import com.nova.ai.domain.tools.AppLauncherTool
import kotlinx.coroutines.delay
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

enum class TaskStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED
}

data class TaskStep(
    val id: String = java.util.UUID.randomUUID().toString(),
    val action: String,
    val parameter: String,
    var status: TaskStatus = TaskStatus.PENDING,
    var errorMessage: String? = null
)

data class ExecutionPlan(
    val originalCommand: String,
    val steps: List<TaskStep>
)

@Singleton
class TaskPlanner @Inject constructor(
    private val aiRepository: AIRepository,
    private val appLauncherTool: AppLauncherTool
) {
    private var isCancelled = false

    suspend fun decompose(userCommand: String): ExecutionPlan {
        isCancelled = false
        val planJson = aiRepository.getDecomposedPlan(userCommand)
        val steps = mutableListOf<TaskStep>()

        try {
            val root = JSONObject(planJson)
            val jsonSteps = root.getJSONArray("steps")
            for (i in 0 until jsonSteps.length()) {
                val item = jsonSteps.getJSONObject(i)
                steps.add(
                    TaskStep(
                        action = item.getString("action"),
                        parameter = item.getString("parameter")
                    )
                )
            }
        } catch (_: Exception) {
            if (userCommand.lowercase().startsWith("open")) {
                val app = userCommand.removePrefix("open").trim()
                steps.add(TaskStep(action = "OPEN_APP", parameter = app))
            }
        }

        return ExecutionPlan(originalCommand = userCommand, steps = steps)
    }

    suspend fun executePlan(plan: ExecutionPlan) {
        val accessibilityService = AccessibilityAutomationService.instance

        for (step in plan.steps) {
            if (isCancelled) {
                step.status = TaskStatus.CANCELLED
                break
            }

            step.status = TaskStatus.RUNNING

            when (step.action) {
                "OPEN_APP" -> {
                    val result = appLauncherTool.execute(step.parameter)
                    if (result.success) {
                        step.status = TaskStatus.COMPLETED
                    } else {
                        step.status = TaskStatus.FAILED
                        step.errorMessage = result.outputMessage
                    }
                }
                "WAIT" -> {
                    val duration = step.parameter.toLongOrNull() ?: 2000L
                    delay(duration)
                    step.status = TaskStatus.COMPLETED
                }
                "CLICK_ELEMENT" -> {
                    val clicked = accessibilityService?.findAndClickElementByText(step.parameter) ?: false
                    step.status = if (clicked) TaskStatus.COMPLETED else TaskStatus.FAILED
                }
                "SCROLL_DOWN" -> {
                    val scrolled = accessibilityService?.scroll(forward = true) ?: false
                    step.status = if (scrolled) TaskStatus.COMPLETED else TaskStatus.FAILED
                }
                else -> {
                    step.status = TaskStatus.FAILED
                    step.errorMessage = "Unsupported action: ${step.action}"
                }
            }

            delay(600)
        }
    }

    fun abortCurrentPlan() {
        isCancelled = true
    }
}
