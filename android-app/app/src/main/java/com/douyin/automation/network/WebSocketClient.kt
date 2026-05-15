package com.douyin.automation.network

import kotlinx.coroutines.*
import okhttp3.*
import java.util.concurrent.TimeUnit

class WebSocketClient(
    private val url: String,
    private val onConnected: () -> Unit,
    private val onMessage: (String) -> Unit,
    private val onDisconnected: () -> Unit,
    private val onError: (String) -> Unit
) {
    
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS)
        .build()
    
    private var heartbeatJob: Job? = null
    
    fun connect() {
        val request = Request.Builder()
            .url(url)
            .build()
        
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                // 发送注册消息
                val deviceId = "android_${System.currentTimeMillis()}"
                val registerMsg = """{"type":"register","device_id":"$deviceId"}"""
                webSocket.send(registerMsg)
                onConnected()
                startHeartbeat()
            }
            
            override fun onMessage(webSocket: WebSocket, text: String) {
                onMessage(text)
            }
            
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                stopHeartbeat()
                onDisconnected()
            }
            
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                stopHeartbeat()
                onError(t.message ?: "连接失败")
            }
        })
    }
    
    fun sendMessage(content: String) {
        // 按照协议格式发送消息
        val message = """{"type":"message","content":"$content"}"""
        webSocket?.send(message)
    }
    
    fun disconnect() {
        stopHeartbeat()
        webSocket?.close(1000, "主动断开")
        webSocket = null
    }
    
    private fun startHeartbeat() {
        heartbeatJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                delay(30000) // 30秒
                val heartbeat = """{"type":"heartbeat","timestamp":${System.currentTimeMillis()}}"""
                webSocket?.send(heartbeat)
            }
        }
    }
    
    private fun stopHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = null
    }
}
