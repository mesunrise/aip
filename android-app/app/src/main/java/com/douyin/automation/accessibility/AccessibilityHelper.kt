package com.douyin.automation.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log

/**
 * 无障碍服务辅助工具类
 */
object AccessibilityHelper {
    
    private const val TAG = "AccessibilityHelper"
    
    /**
     * 查找所有匹配的节点
     */
    fun findAllNodes(
        root: AccessibilityNodeInfo?,
        predicate: (AccessibilityNodeInfo) -> Boolean
    ): List<AccessibilityNodeInfo> {
        val result = mutableListOf<AccessibilityNodeInfo>()
        if (root == null) return result
        
        findNodesRecursive(root, predicate, result)
        return result
    }
    
    private fun findNodesRecursive(
        node: AccessibilityNodeInfo,
        predicate: (AccessibilityNodeInfo) -> Boolean,
        result: MutableList<AccessibilityNodeInfo>
    ) {
        if (predicate(node)) {
            result.add(node)
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            findNodesRecursive(child, predicate, result)
        }
    }
    
    /**
     * 查找第一个匹配的节点
     */
    fun findFirstNode(
        root: AccessibilityNodeInfo?,
        predicate: (AccessibilityNodeInfo) -> Boolean
    ): AccessibilityNodeInfo? {
        if (root == null) return null
        
        if (predicate(root)) return root
        
        for (i in 0 until root.childCount) {
            val child = root.getChild(i) ?: continue
            val result = findFirstNode(child, predicate)
            if (result != null) return result
        }
        
        return null
    }
    
    /**
     * 点击节点
     */
    fun clickNode(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false
        
        return if (node.isClickable) {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        } else {
            // 如果节点不可点击，尝试点击父节点
            clickNode(node.parent)
        }
    }
    
    /**
     * 输入文本
     */
    fun inputText(node: AccessibilityNodeInfo?, text: String): Boolean {
        if (node == null) return false
        
        // 先点击获取焦点
        node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        Thread.sleep(300)
        
        // 清除现有文本
        node.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
        val arguments = android.os.Bundle().apply {
            putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                text
            )
        }
        
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
    }
    
    /**
     * 获取节点文本
     */
    fun getNodeText(node: AccessibilityNodeInfo?): String? {
        return node?.text?.toString()
    }
    
    /**
     * 获取节点描述
     */
    fun getNodeDescription(node: AccessibilityNodeInfo?): String? {
        return node?.contentDescription?.toString()
    }
    
    /**
     * 打印节点树（用于调试）
     */
    fun printNodeTree(node: AccessibilityNodeInfo?, indent: String = "") {
        if (node == null) return
        
        val text = getNodeText(node) ?: ""
        val desc = getNodeDescription(node) ?: ""
        val id = node.viewIdResourceName ?: ""
        val className = node.className ?: ""
        
        Log.d(TAG, "$indent[$className] id=$id text=$text desc=$desc")
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            printNodeTree(child, "$indent  ")
        }
    }
    
    /**
     * 滚动到底部
     */
    fun scrollToBottom(node: AccessibilityNodeInfo?): Boolean {
        return node?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) ?: false
    }
    
    /**
     * 滚动到顶部
     */
    fun scrollToTop(node: AccessibilityNodeInfo?): Boolean {
        return node?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) ?: false
    }
    
    /**
     * 检查节点是否可见
     */
    fun isNodeVisible(node: AccessibilityNodeInfo?): Boolean {
        return node?.isVisibleToUser ?: false
    }
    
    /**
     * 检查节点是否可点击
     */
    fun isNodeClickable(node: AccessibilityNodeInfo?): Boolean {
        return node?.isClickable ?: false
    }
}
