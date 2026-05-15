package com.douyin.automation.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.douyin.automation.douyin.DouyinLauncher
import com.douyin.automation.douyin.DouyinNavigator
import com.douyin.automation.locator.ElementLocator
import com.douyin.automation.logger.OperationLogger
import com.douyin.automation.task.SearchBloggerTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 自动化无障碍服务
 * 用于实现抖音自动化操作
 */
class AutomationAccessibilityService : AccessibilityService() {
    
    companion object {
        private const val TAG = "AutomationService"
        const val ACTION_SERVICE_CONNECTED = "com.douyin.automation.SERVICE_CONNECTED"
        const val ACTION_EXECUTE_SEARCH = "com.douyin.automation.EXECUTE_SEARCH"
        const val EXTRA_BLOGGER_NAME = "blogger_name"
        
        // 静态实例，用于外部访问
        @Volatile
        private var instance: AutomationAccessibilityService? = null
        
        fun getInstance(): AutomationAccessibilityService? = instance
        
        fun isServiceRunning(): Boolean = instance != null
    }
    
    private lateinit var elementLocator: ElementLocator
    private lateinit var douyinLauncher: DouyinLauncher
    private lateinit var douyinNavigator: DouyinNavigator
    private lateinit var operationLogger: OperationLogger
    
    private var currentTask: SearchBloggerTask? = null
    private var wsClient: com.douyin.automation.network.WebSocketClient? = null
    
    /**
     * 设置WebSocket客户端
     */
    fun setWebSocketClient(client: com.douyin.automation.network.WebSocketClient?) {
        wsClient = client
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        
        Log.d(TAG, "✅ 无障碍服务已连接")
        
        // 初始化组件
        elementLocator = ElementLocator(this)
        douyinLauncher = DouyinLauncher(this)
        douyinNavigator = DouyinNavigator(this, elementLocator)
        operationLogger = OperationLogger()
        
        // 通知MainActivity服务已启动
        sendBroadcast(Intent(ACTION_SERVICE_CONNECTED))
        
        Log.d(TAG, "📱 服务初始化完成")
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                handleWindowStateChanged(event)
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                handleContentChanged(event)
            }
        }
    }
    
    override fun onInterrupt() {
        Log.w(TAG, "⚠️ 无障碍服务中断")
    }
    
    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        Log.d(TAG, "❌ 无障碍服务已断开")
        return super.onUnbind(intent)
    }
    
    /**
     * 处理窗口状态变化
     */
    private fun handleWindowStateChanged(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        val className = event.className?.toString() ?: return
        
        Log.d(TAG, "🔄 窗口变化: $packageName - $className")
        
        // 检测抖音界面
        if (packageName == "com.ss.android.ugc.aweme") {
            onDouyinWindowChanged(className)
        }
    }
    
    /**
     * 处理内容变化
     */
    private fun handleContentChanged(event: AccessibilityEvent) {
        // 可以在这里监听特定内容的变化
        // 例如：搜索结果加载完成、主页加载完成等
    }
    
    /**
     * 抖音窗口变化回调
     */
    private fun onDouyinWindowChanged(className: String) {
        Log.d(TAG, "📱 抖音界面: $className")
        
        // 通知导航器界面已变化
        douyinNavigator.onWindowChanged(className)
    }
    
    /**
     * 执行搜索博主任务
     */
    fun executeSearchTask(bloggerName: String) {
        Log.d(TAG, "🎯 开始执行搜索任务: $bloggerName")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                operationLogger.log("开始", "搜索博主: $bloggerName", true)
                
                // 创建任务
                currentTask = SearchBloggerTask(
                    service = this@AutomationAccessibilityService,
                    launcher = douyinLauncher,
                    navigator = douyinNavigator,
                    logger = operationLogger,
                    wsClient = wsClient
                )
                
                // 执行任务
                val success = currentTask?.execute(bloggerName) ?: false
                
                if (success) {
                    Log.d(TAG, "✅ 搜索任务完成")
                    operationLogger.log("完成", "任务成功", true)
                } else {
                    Log.e(TAG, "❌ 搜索任务失败")
                    operationLogger.log("完成", "任务失败", false)
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ 任务执行异常", e)
                operationLogger.log("异常", "执行失败", false, e.message)
            } finally {
                currentTask = null
            }
        }
    }
    
    /**
     * 取消当前任务
     */
    fun cancelCurrentTask() {
        currentTask?.cancel()
        currentTask = null
        Log.d(TAG, "🛑 任务已取消")
    }
    
    /**
     * 获取根节点
     */
    fun getRootNode(): AccessibilityNodeInfo? {
        return rootInActiveWindow
    }
    
    /**
     * 执行全局返回操作
     */
    fun performGlobalBack(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_BACK)
    }
    
    /**
     * 执行全局Home操作
     */
    fun performGlobalHome(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_HOME)
    }
    
    /**
     * 执行全局最近任务操作
     */
    fun performGlobalRecents(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_RECENTS)
    }
}
