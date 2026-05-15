package com.douyin.automation.logger

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

/**
 * 操作日志记录器
 */
class OperationLogger {
    
    companion object {
        private const val TAG = "OperationLogger"
    }
    
    data class LogEntry(
        val timestamp: Long,
        val step: String,
        val action: String,
        val result: Boolean,
        val message: String? = null,
        val screenshot: String? = null
    )
    
    private val logs = mutableListOf<LogEntry>()
    
    /**
     * 记录日志
     */
    fun log(step: String, action: String, result: Boolean, message: String? = null) {
        val entry = LogEntry(
            timestamp = System.currentTimeMillis(),
            step = step,
            action = action,
            result = result,
            message = message,
            screenshot = null  // 暂时不实现截图
        )
        
        logs.add(entry)
        
        val emoji = if (result) "✅" else "❌"
        val msg = if (message != null) " - $message" else ""
        Log.d(TAG, "$emoji [$step] $action$msg")
    }
    
    /**
     * 获取所有日志
     */
    fun getAllLogs(): List<LogEntry> {
        return logs.toList()
    }
    
    /**
     * 清空日志
     */
    fun clear() {
        logs.clear()
    }
    
    /**
     * 转换为JSON
     */
    fun toJson(): JSONArray {
        val jsonArray = JSONArray()
        
        for (log in logs) {
            val jsonObject = JSONObject().apply {
                put("timestamp", log.timestamp)
                put("step", log.step)
                put("action", log.action)
                put("result", log.result)
                if (log.message != null) {
                    put("message", log.message)
                }
                if (log.screenshot != null) {
                    put("screenshot", log.screenshot)
                }
            }
            jsonArray.put(jsonObject)
        }
        
        return jsonArray
    }
    
    /**
     * 获取日志摘要
     */
    fun getSummary(): String {
        val total = logs.size
        val success = logs.count { it.result }
        val failed = total - success
        
        return "总计: $total, 成功: $success, 失败: $failed"
    }
}
