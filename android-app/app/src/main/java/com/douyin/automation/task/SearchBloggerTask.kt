package com.douyin.automation.task

import android.util.Log
import com.douyin.automation.accessibility.AutomationAccessibilityService
import com.douyin.automation.douyin.DouyinLauncher
import com.douyin.automation.douyin.DouyinNavigator
import com.douyin.automation.logger.OperationLogger
import com.douyin.automation.network.WebSocketClient
import kotlinx.coroutines.delay
import org.json.JSONObject

/**
 * 搜索博主任务
 */
class SearchBloggerTask(
    private val service: AutomationAccessibilityService,
    private val launcher: DouyinLauncher,
    private val navigator: DouyinNavigator,
    private val logger: OperationLogger,
    private val wsClient: WebSocketClient? = null
) {
    
    companion object {
        private const val TAG = "SearchBloggerTask"
    }
    
    private var isCancelled = false
    
    /**
     * 执行任务
     */
    suspend fun execute(bloggerName: String): Boolean {
        try {
            logger.clear()
            logger.log("开始", "搜索博主: $bloggerName", true)
            
            // 1. 启动抖音
            if (isCancelled) return false
            if (!launchDouyinApp()) {
                return false
            }
            
            // 2. 搜索博主
            if (isCancelled) return false
            if (!searchBlogger(bloggerName)) {
                return false
            }
            
            // 3. 进入主页
            if (isCancelled) return false
            if (!enterProfile(bloggerName)) {
                return false
            }
            
            // 4. 上报结果
            if (isCancelled) return false
            reportSuccess(bloggerName)
            
            logger.log("完成", "任务成功", true)
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "任务执行异常", e)
            logger.log("异常", "执行失败", false, e.message)
            reportFailure(bloggerName, e.message ?: "未知错误")
            return false
        }
    }
    
    /**
     * 启动抖音
     */
    private suspend fun launchDouyinApp(): Boolean {
        logger.log("启动抖音", "检查安装", true)
        
        if (!launcher.isDouyinInstalled()) {
            logger.log("启动抖音", "失败", false, "抖音未安装")
            return false
        }
        
        logger.log("启动抖音", "启动应用", true)
        
        if (!launcher.launchDouyin()) {
            logger.log("启动抖音", "失败", false, "无法启动抖音")
            return false
        }
        
        logger.log("启动抖音", "成功", true)
        delay(3000)  // 等待抖音加载
        
        return true
    }
    
    /**
     * 搜索博主
     */
    private suspend fun searchBlogger(bloggerName: String): Boolean {
        logger.log("搜索博主", "开始搜索", true)
        
        if (!navigator.searchBlogger(bloggerName)) {
            logger.log("搜索博主", "失败", false, "搜索操作失败")
            return false
        }
        
        logger.log("搜索博主", "成功", true)
        return true
    }
    
    /**
     * 进入主页
     */
    private suspend fun enterProfile(bloggerName: String): Boolean {
        logger.log("进入主页", "查找博主", true)
        
        if (!navigator.enterProfile(bloggerName)) {
            logger.log("进入主页", "失败", false, "无法进入主页")
            return false
        }
        
        logger.log("进入主页", "成功", true)
        return true
    }
    
    /**
     * 上报成功结果
     */
    private fun reportSuccess(bloggerName: String) {
        try {
            if (wsClient == null) {
                Log.w(TAG, "WebSocket未连接，无法上报结果")
                return
            }
            
            val result = JSONObject().apply {
                put("type", "search_result")
                put("success", true)
                put("blogger_name", bloggerName)
                put("timestamp", System.currentTimeMillis())
                put("logs", logger.toJson())
            }
            
            wsClient.sendMessage(result.toString())
            Log.d(TAG, "✅ 结果已上报")
            
        } catch (e: Exception) {
            Log.e(TAG, "上报结果失败", e)
        }
    }
    
    /**
     * 上报失败结果
     */
    private fun reportFailure(bloggerName: String, error: String) {
        try {
            if (wsClient == null) {
                Log.w(TAG, "WebSocket未连接，无法上报结果")
                return
            }
            
            val result = JSONObject().apply {
                put("type", "search_result")
                put("success", false)
                put("blogger_name", bloggerName)
                put("error", error)
                put("timestamp", System.currentTimeMillis())
                put("logs", logger.toJson())
            }
            
            wsClient.sendMessage(result.toString())
            Log.d(TAG, "❌ 失败结果已上报")
            
        } catch (e: Exception) {
            Log.e(TAG, "上报失败结果失败", e)
        }
    }
    
    /**
     * 取消任务
     */
    fun cancel() {
        isCancelled = true
        logger.log("取消", "任务已取消", true)
    }
}
