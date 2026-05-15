package com.douyin.automation.locator

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log
import com.douyin.automation.accessibility.AccessibilityHelper

/**
 * 元素定位器
 */
class ElementLocator(private val service: AccessibilityService) {
    
    companion object {
        private const val TAG = "ElementLocator"
    }
    
    /**
     * 通过ID查找元素（带超时）
     */
    fun findById(id: String, timeout: Long = 5000): AccessibilityNodeInfo? {
        val startTime = System.currentTimeMillis()
        
        while (System.currentTimeMillis() - startTime < timeout) {
            val root = service.rootInActiveWindow
            if (root != null) {
                val node = findNodeById(root, id)
                if (node != null) {
                    Log.d(TAG, "✅ 找到元素 (ID): $id")
                    return node
                }
            }
            Thread.sleep(500)
        }
        
        Log.w(TAG, "⚠️ 未找到元素 (ID): $id")
        return null
    }
    
    /**
     * 通过文本查找元素
     */
    fun findByText(text: String, exact: Boolean = false, timeout: Long = 5000): AccessibilityNodeInfo? {
        val startTime = System.currentTimeMillis()
        
        while (System.currentTimeMillis() - startTime < timeout) {
            val root = service.rootInActiveWindow
            if (root != null) {
                val node = findNodeByText(root, text, exact)
                if (node != null) {
                    Log.d(TAG, "✅ 找到元素 (文本): $text")
                    return node
                }
            }
            Thread.sleep(500)
        }
        
        Log.w(TAG, "⚠️ 未找到元素 (文本): $text")
        return null
    }
    
    /**
     * 通过类名查找元素
     */
    fun findByClassName(className: String, timeout: Long = 5000): List<AccessibilityNodeInfo> {
        val startTime = System.currentTimeMillis()
        
        while (System.currentTimeMillis() - startTime < timeout) {
            val root = service.rootInActiveWindow
            if (root != null) {
                val nodes = findNodesByClassName(root, className)
                if (nodes.isNotEmpty()) {
                    Log.d(TAG, "✅ 找到 ${nodes.size} 个元素 (类名): $className")
                    return nodes
                }
            }
            Thread.sleep(500)
        }
        
        Log.w(TAG, "⚠️ 未找到元素 (类名): $className")
        return emptyList()
    }
    
    /**
     * 通过描述查找元素
     */
    fun findByDescription(desc: String, exact: Boolean = false, timeout: Long = 5000): AccessibilityNodeInfo? {
        val startTime = System.currentTimeMillis()
        
        while (System.currentTimeMillis() - startTime < timeout) {
            val root = service.rootInActiveWindow
            if (root != null) {
                val node = findNodeByDescription(root, desc, exact)
                if (node != null) {
                    Log.d(TAG, "✅ 找到元素 (描述): $desc")
                    return node
                }
            }
            Thread.sleep(500)
        }
        
        Log.w(TAG, "⚠️ 未找到元素 (描述): $desc")
        return null
    }
    
    /**
     * 查找所有匹配文本的元素
     */
    fun findAllByText(text: String, exact: Boolean = false): List<AccessibilityNodeInfo> {
        val root = service.rootInActiveWindow ?: return emptyList()
        return findAllNodesByText(root, text, exact)
    }
    
    /**
     * 递归查找节点（通过ID）
     */
    private fun findNodeById(node: AccessibilityNodeInfo, id: String): AccessibilityNodeInfo? {
        if (node.viewIdResourceName == id) {
            return node
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findNodeById(child, id)
            if (result != null) return result
        }
        
        return null
    }
    
    /**
     * 递归查找节点（通过文本）
     */
    private fun findNodeByText(node: AccessibilityNodeInfo, text: String, exact: Boolean): AccessibilityNodeInfo? {
        val nodeText = node.text?.toString()
        
        if (nodeText != null) {
            val matches = if (exact) {
                nodeText == text
            } else {
                nodeText.contains(text, ignoreCase = true)
            }
            
            if (matches) return node
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findNodeByText(child, text, exact)
            if (result != null) return result
        }
        
        return null
    }
    
    /**
     * 递归查找所有匹配文本的节点
     */
    private fun findAllNodesByText(node: AccessibilityNodeInfo, text: String, exact: Boolean): List<AccessibilityNodeInfo> {
        val results = mutableListOf<AccessibilityNodeInfo>()
        
        val nodeText = node.text?.toString()
        if (nodeText != null) {
            val matches = if (exact) {
                nodeText == text
            } else {
                nodeText.contains(text, ignoreCase = true)
            }
            
            if (matches) results.add(node)
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            results.addAll(findAllNodesByText(child, text, exact))
        }
        
        return results
    }
    
    /**
     * 递归查找节点（通过类名）
     */
    private fun findNodesByClassName(node: AccessibilityNodeInfo, className: String): List<AccessibilityNodeInfo> {
        val results = mutableListOf<AccessibilityNodeInfo>()
        
        if (node.className?.toString() == className) {
            results.add(node)
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            results.addAll(findNodesByClassName(child, className))
        }
        
        return results
    }
    
    /**
     * 递归查找节点（通过描述）
     */
    private fun findNodeByDescription(node: AccessibilityNodeInfo, desc: String, exact: Boolean): AccessibilityNodeInfo? {
        val nodeDesc = node.contentDescription?.toString()
        
        if (nodeDesc != null) {
            val matches = if (exact) {
                nodeDesc == desc
            } else {
                nodeDesc.contains(desc, ignoreCase = true)
            }
            
            if (matches) return node
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findNodeByDescription(child, desc, exact)
            if (result != null) return result
        }
        
        return null
    }
    
    /**
     * 等待元素出现
     */
    fun waitForElement(
        predicate: () -> AccessibilityNodeInfo?,
        timeout: Long = 10000
    ): AccessibilityNodeInfo? {
        val startTime = System.currentTimeMillis()
        
        while (System.currentTimeMillis() - startTime < timeout) {
            val node = predicate()
            if (node != null) return node
            Thread.sleep(500)
        }
        
        return null
    }
    
    /**
     * 打印当前界面的节点树（用于调试）
     */
    fun printCurrentTree() {
        val root = service.rootInActiveWindow
        if (root != null) {
            Log.d(TAG, "========== 节点树 ==========")
            AccessibilityHelper.printNodeTree(root)
            Log.d(TAG, "============================")
        } else {
            Log.w(TAG, "无法获取根节点")
        }
    }
}
