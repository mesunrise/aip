package com.douyin.automation

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.douyin.automation.network.WebSocketClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var wsClient: WebSocketClient
    private lateinit var statusText: TextView
    private lateinit var logText: TextView
    private lateinit var serverUrlInput: EditText
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // 初始化UI
        statusText = findViewById(R.id.statusText)
        logText = findViewById(R.id.logText)
        serverUrlInput = findViewById(R.id.serverUrlInput)
        
        val connectBtn = findViewById<Button>(R.id.connectBtn)
        val disconnectBtn = findViewById<Button>(R.id.disconnectBtn)
        val sendBtn = findViewById<Button>(R.id.sendBtn)
        val messageInput = findViewById<EditText>(R.id.messageInput)
        
        // 默认服务器地址
        serverUrlInput.setText("ws://10.0.2.2:8000/ws/android_001")
        
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
    }
    
    private fun connectToServer(url: String) {
        wsClient = WebSocketClient(
            url = url,
            onConnected = {
                runOnUiThread {
                    updateStatus("已连接")
                    addLog("✅ 连接成功")
                }
            },
            onMessage = { message ->
                runOnUiThread {
                    addLog("📨 收到: $message")
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
    
    private fun updateStatus(status: String) {
        statusText.text = "状态: $status"
    }
    
    private fun addLog(log: String) {
        val currentLog = logText.text.toString()
        logText.text = "$log\n$currentLog"
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (::wsClient.isInitialized) {
            wsClient.disconnect()
        }
    }
}
