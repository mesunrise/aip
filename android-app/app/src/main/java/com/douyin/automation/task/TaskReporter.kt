package com.douyin.automation.task

import android.util.Log
import com.douyin.automation.logger.OperationLogger
import com.douyin.automation.network.WebSocketClient
import org.json.JSONObject

/**
 * WebSocket任务结果上报器
 */
class TaskReporter(
    private val wsClient: WebSocketClient? = null,
    private val logger: OperationLogger
) {

    companion object {
        private const val TAG = "TaskReporter"
    }

    fun reportStepResult(taskId: String, stepIndex: Int, success: Boolean, message: String) {
        val payload = JSONObject().apply {
            put("type", "step_result")
            put("task_id", taskId)
            put("step_index", stepIndex)
            put("success", success)
            put("message", message)
        }
        send(payload, "步骤结果")
    }

    fun reportTaskResult(taskId: String, success: Boolean, result: JSONObject) {
        val payload = JSONObject().apply {
            put("type", "task_result")
            put("task_id", taskId)
            put("success", success)
            put("result", result)
        }
        send(payload, "任务结果")
    }

    fun buildTaskResultPayload(
        taskType: String,
        target: String,
        success: Boolean,
        error: String? = null
    ): JSONObject {
        return JSONObject().apply {
            put("task_type", taskType)
            put("target", target)
            put("success", success)
            put("timestamp", System.currentTimeMillis())
            put("logs", logger.toJson())
            if (!error.isNullOrBlank()) {
                put("error", error)
            }
        }
    }

    private fun send(payload: JSONObject, label: String) {
        if (wsClient == null) {
            Log.w(TAG, "WebSocket未连接，无法上报$label")
            return
        }

        try {
            wsClient.sendRawMessage(payload.toString())
            Log.d(TAG, "✅ ${label}已上报: $payload")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ${label}上报失败", e)
        }
    }
}
