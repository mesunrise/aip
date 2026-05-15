package com.douyin.automation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.douyin.automation.accessibility.AutomationAccessibilityService
import com.douyin.automation.network.WebSocketClient
import com.douyin.automation.permission.PermissionChecker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

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
                "search_blogger" -> {
                    val bloggerName = json.optString("blogger_name", "")
                    if (bloggerName.isNotEmpty()) {
                        addLog("🎯 收到搜索指令: $bloggerName")
                        executeSearch(bloggerName)
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
     * 执行搜索任务
     */
    private fun executeSearch(bloggerName: String) {
        val service = AutomationAccessibilityService.getInstance()
        
        if (service == null) {
            addLog("❌ 无障碍服务未启动")
            showAccessibilityDialog()
            return
        }
        
        addLog("🚀 开始执行搜索任务...")
        service.executeSearchTask(bloggerName)
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
                    executeSearch(bloggerName)
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
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
