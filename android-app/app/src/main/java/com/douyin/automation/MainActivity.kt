package com.douyin.automation

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.douyin.automation.accessibility.AutomationAccessibilityService
import com.douyin.automation.network.WebSocketClient
import com.douyin.automation.permission.PermissionChecker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.URL

class MainActivity : AppCompatActivity() {
    
    private lateinit var wsClient: WebSocketClient
    private lateinit var statusText: TextView
    private lateinit var logText: TextView
    private lateinit var serverUrlInput: EditText
    private lateinit var accessibilityStatusText: TextView
    
    private val serviceConnectedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AutomationAccessibilityService.ACTION_SERVICE_CONNECTED) {
                updateAccessibilityStatus()
                addLog("✅ 无障碍服务已启动")
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // 初始化UI
        statusText = findViewById(R.id.statusText)
        logText = findViewById(R.id.logText)
        serverUrlInput = findViewById(R.id.serverUrlInput)
        accessibilityStatusText = findViewById(R.id.accessibilityStatusText)
        
        val connectBtn = findViewById<Button>(R.id.connectBtn)
        val disconnectBtn = findViewById<Button>(R.id.disconnectBtn)
        val sendBtn = findViewById<Button>(R.id.sendBtn)
        val messageInput = findViewById<EditText>(R.id.messageInput)
        val checkAccessibilityBtn = findViewById<Button>(R.id.checkAccessibilityBtn)
        val startTaskBtn = findViewById<Button>(R.id.startTaskBtn)
        val updateBtn = findViewById<Button>(R.id.updateBtn)
        val testSearchBtn = findViewById<Button>(R.id.testSearchBtn)
        
        // 默认服务器地址
        serverUrlInput.setText("ws://elxn1431783.bohrium.tech:50002/ws")
        
        // 连接按钮
        connectBtn.setOnClickListener {
            val url = serverUrlInput.text.toString()
            connectToServer(url)
        }
        
        // 断开按钮
        disconnectBtn.setOnClickListener {
            wsClient.disconnect()
            updateStatus("已断开")
        }
        
        // 发送按钮
        sendBtn.setOnClickListener {
            val message = messageInput.text.toString()
            if (message.isNotEmpty()) {
                wsClient.sendMessage(message)
                addLog("发送: $message")
                messageInput.setText("")
            }
        }
        
        // 检查无障碍权限按钮
        checkAccessibilityBtn.setOnClickListener {
            checkAndRequestAccessibility()
        }
        
        // 开始任务按钮
        startTaskBtn.setOnClickListener {
            startTask()
        }
        
        // 检查更新按钮
        updateBtn.setOnClickListener {
            addLog("⚠️ 更新功能开发中")
            addLog("📥 请从GitHub下载最新版本")
            addLog("🔗 https://github.com/mesunrise/aip/actions")
        }
        
        // 测试搜索按钮
        testSearchBtn.setOnClickListener {
            testSearch()
        }
        
        // 注册广播接收器
        val filter = IntentFilter(AutomationAccessibilityService.ACTION_SERVICE_CONNECTED)
        registerReceiver(serviceConnectedReceiver, filter)
        
        // 检查无障碍权限状态
        updateAccessibilityStatus()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(serviceConnectedReceiver)
    }
    
    override fun onResume() {
        super.onResume()
        updateAccessibilityStatus()
    }
    
    private fun connectToServer(url: String) {
        wsClient = WebSocketClient(
            url = url,
            onConnected = {
                runOnUiThread {
                    updateStatus("已连接")
                    addLog("✅ 连接成功")
                    
                    // 将WebSocket客户端传递给无障碍服务
                    AutomationAccessibilityService.getInstance()?.setWebSocketClient(wsClient)
                }
            },
            onMessage = { message ->
                runOnUiThread {
                    addLog("📨 收到: $message")
                    handleServerMessage(message)
                }
            },
            onDisconnected = {
                runOnUiThread {
                    updateStatus("已断开")
                    addLog("❌ 连接断开")
                }
            },
            onError = { error ->
                runOnUiThread {
                    updateStatus("错误")
                    addLog("⚠️ 错误: $error")
                }
            }
        )
        
        CoroutineScope(Dispatchers.IO).launch {
            wsClient.connect()
        }
        
        updateStatus("连接中...")
    }
    
    /**
     * 处理服务器消息
     */
    private fun handleServerMessage(message: String) {
        try {
            val json = JSONObject(message)
            val type = json.optString("type", "")
            
            when (type) {
                "task_start" -> {
                    val taskId = json.optString("task_id", "")
                    val taskName = json.optString("task_name", "")
                    val totalSteps = json.optInt("total_steps", 0)
                    addLog("🎯 开始任务: $taskName (ID: $taskId)")
                    addLog("📝 共 $totalSteps 个步骤")
                }
                
                "step" -> {
                    val taskId = json.optString("task_id", "")
                    val stepIndex = json.optInt("step_index", 0)
                    val action = json.optString("action", "")
                    addLog("📍 步骤 $stepIndex: $action")
                    executeStep(taskId, stepIndex, action, json)
                }
                
                "no_task" -> {
                    addLog("⚠️ 没有待执行的任务")
                }
                
                "search_blogger" -> {
                    val taskId = json.optString("task_id", "")
                    val bloggerName = json.optString("blogger_name", "")
                    if (bloggerName.isNotEmpty()) {
                        addLog("🎯 收到搜索指令: $bloggerName")
                        executeSearch(taskId.ifBlank { "search_blogger_${System.currentTimeMillis()}" }, bloggerName)
                    }
                }
                
                "message_ack" -> {
                    addLog("✅ 服务器确认收到消息")
                }
            }
        } catch (e: Exception) {
            addLog("⚠️ 解析消息失败: ${e.message}")
        }
    }
    
    /**
     * 开始任务
     */
    private fun startTask() {
        if (!::wsClient.isInitialized) {
            addLog("❌ 请先连接服务器")
            return
        }
        
        val service = AutomationAccessibilityService.getInstance()
        if (service == null) {
            addLog("❌ 无障碍服务未启动")
            showAccessibilityDialog()
            return
        }
        
        addLog("🚀 请求开始任务...")
        val message = """{"type":"start_task"}"""
        wsClient.sendRawMessage(message)
    }
    
    /**
     * 执行步骤
     */
    private fun executeStep(taskId: String, stepIndex: Int, action: String, json: JSONObject) {
        val service = AutomationAccessibilityService.getInstance()

        if (service == null) {
            addLog("❌ 无障碍服务未启动")
            reportStepResult(taskId, stepIndex, false, "无障碍服务未启动")
            return
        }

        addLog("⚙️ 执行: $action")

        when (action) {
            "search_keyword" -> {
                val keyword = json.optString("keyword", "")
                addLog("🔍 搜索关键词: $keyword")
                CoroutineScope(Dispatchers.IO).launch {
                    val success = service.searchKeyword(keyword)
                    val message = if (success) "搜索完成" else "搜索失败"
                    runOnUiThread { reportStepResult(taskId, stepIndex, success, message) }
                }
            }

            "enter_first_video_author" -> {
                addLog("👤 进入第一个作品的博主主页")
                CoroutineScope(Dispatchers.IO).launch {
                    val success = service.enterFirstVideoAuthor()
                    val message = if (success) "已进入第一个作品作者主页" else "进入第一个作品作者主页失败"
                    runOnUiThread { reportStepResult(taskId, stepIndex, success, message) }
                }
            }

            "scroll_profile" -> {
                val scrollCount = json.optInt("scroll_count", 3)
                addLog("📜 滑动主页 $scrollCount 次")
                CoroutineScope(Dispatchers.IO).launch {
                    val success = service.scrollProfile(scrollCount)
                    val message = if (success) "主页滑动完成" else "主页滑动失败"
                    runOnUiThread { reportStepResult(taskId, stepIndex, success, message) }
                }
            }

            "enter_first_video" -> {
                addLog("🎬 进入第一个作品")
                CoroutineScope(Dispatchers.IO).launch {
                    val success = service.enterFirstVideo()
                    val message = if (success) "已进入第一个作品" else "进入第一个作品失败"
                    runOnUiThread { reportStepResult(taskId, stepIndex, success, message) }
                }
            }

            "return_to_app" -> {
                addLog("🔙 返回App主界面")
                CoroutineScope(Dispatchers.IO).launch {
                    val success = service.returnToAppHome()
                    val message = if (success) "已返回App主界面" else "返回App主界面失败"
                    runOnUiThread { reportStepResult(taskId, stepIndex, success, message) }
                }
            }

            else -> {
                addLog("⚠️ 未知动作: $action")
                reportStepResult(taskId, stepIndex, false, "未知动作: $action")
            }
        }
    }
    
    /**
     * 上报步骤结果
     */
    private fun reportStepResult(taskId: String, stepIndex: Int, success: Boolean, message: String) {
        if (!::wsClient.isInitialized) return
        
        val result = """
            {
                "type": "step_result",
                "task_id": "$taskId",
                "step_index": $stepIndex,
                "success": $success,
                "message": "$message"
            }
        """.trimIndent()
        
        wsClient.sendRawMessage(result)
        
        val status = if (success) "✅" else "❌"
        addLog("$status 步骤 $stepIndex: $message")
    }
    
    /**
     * 上报任务结果
     */
    private fun reportTaskResult(taskId: String, success: Boolean, result: Map<String, Any>) {
        if (!::wsClient.isInitialized) return
        
        val resultJson = JSONObject(result).toString()
        val message = """
            {
                "type": "task_result",
                "task_id": "$taskId",
                "success": $success,
                "result": $resultJson
            }
        """.trimIndent()
        
        wsClient.sendRawMessage(message)
        
        val status = if (success) "✅" else "❌"
        addLog("$status 任务完成: $taskId")
    }
    
    /**
     * 执行搜索任务
     */
    private fun executeSearch(taskId: String, bloggerName: String) {
        val service = AutomationAccessibilityService.getInstance()
        
        if (service == null) {
            addLog("❌ 无障碍服务未启动")
            showAccessibilityDialog()
            return
        }
        
        addLog("🚀 开始执行搜索任务...")
        service.executeSearchTask(taskId, bloggerName)
    }
    
    /**
     * 测试搜索功能
     */
    private fun testSearch() {
        val input = EditText(this)
        input.hint = "输入博主名称"
        
        AlertDialog.Builder(this)
            .setTitle("测试搜索")
            .setView(input)
            .setPositiveButton("搜索") { _, _ ->
                val bloggerName = input.text.toString()
                if (bloggerName.isNotEmpty()) {
                    executeSearch("manual_test_${System.currentTimeMillis()}", bloggerName)
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    // 更新功能暂时禁用，避免编译错误
    // TODO: 后续优化更新功能
    
    /**
     * 检查并请求无障碍权限
     */
    private fun checkAndRequestAccessibility() {
        if (PermissionChecker.isOurAccessibilityServiceEnabled(this)) {
            addLog("✅ 无障碍权限已开启")
            updateAccessibilityStatus()
        } else {
            showAccessibilityDialog()
        }
    }
    
    /**
     * 显示无障碍权限引导对话框
     */
    private fun showAccessibilityDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.permission_guide_title)
            .setMessage(R.string.permission_guide_message)
            .setPositiveButton(R.string.open_settings) { _, _ ->
                PermissionChecker.openAccessibilitySettings(this)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    /**
     * 更新无障碍权限状态
     */
    private fun updateAccessibilityStatus() {
        val isEnabled = PermissionChecker.isOurAccessibilityServiceEnabled(this)
        val status = if (isEnabled) "✅ 已开启" else "❌ 未开启"
        accessibilityStatusText.text = "无障碍服务: $status"
    }
    
    private fun updateStatus(status: String) {
        statusText.text = "状态: $status"
    }
    
    private fun addLog(log: String) {
        val currentLog = logText.text.toString()
        val newLog = if (currentLog.isEmpty()) {
            log
        } else {
            "$currentLog\n$log"
        }
        logText.text = newLog
        
        // 自动滚动到底部
        logText.post {
            val scrollView = findViewById<android.widget.ScrollView>(R.id.logScrollView)
            scrollView?.fullScroll(android.view.View.FOCUS_DOWN)
        }
    }
}
