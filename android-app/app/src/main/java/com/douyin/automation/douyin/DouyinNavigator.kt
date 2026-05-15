package com.douyin.automation.douyin

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.douyin.automation.locator.ElementLocator
import com.douyin.automation.accessibility.AccessibilityHelper
import kotlinx.coroutines.delay

/**
 * 抖音导航器
 * 负责在抖音App中进行导航操作
 */
class DouyinNavigator(
    private val service: AccessibilityService,
    private val locator: ElementLocator
) {
    
    companion object {
        private const val TAG = "DouyinNavigator"
    }
    
    private var currentScreen: String? = null
    
    /**
     * 窗口变化回调
     */
    fun onWindowChanged(className: String) {
        currentScreen = className
        Log.d(TAG, "当前界面: $className")
    }
    
    /**
     * 搜索博主
     */
    suspend fun searchBlogger(name: String): Boolean {
        Log.d(TAG, "🔍 开始搜索博主: $name")
        
        // 1. 点击搜索按钮
        if (!clickSearchButton()) {
            Log.e(TAG, "❌ 无法点击搜索按钮")
            return false
        }
        Log.d(TAG, "✅ 已点击搜索按钮")
        delay(1500)
        
        // 2. 输入搜索文本
        if (!inputSearchText(name)) {
            Log.e(TAG, "❌ 无法输入搜索文本")
            return false
        }
        Log.d(TAG, "✅ 已输入搜索文本: $name")
        delay(1000)
        
        // 3. 触发搜索
        if (!triggerSearch()) {
            Log.e(TAG, "❌ 无法触发搜索")
            return false
        }
        Log.d(TAG, "✅ 已触发搜索")
        delay(2000)
        
        // 4. 等待搜索结果
        if (!waitForSearchResults()) {
            Log.e(TAG, "❌ 搜索结果未加载")
            return false
        }
        Log.d(TAG, "✅ 搜索结果已加载")
        
        return true
    }
    
    /**
     * 点击搜索按钮
     */
    private fun clickSearchButton(): Boolean {
        // 尝试通过文本查找
        var node = locator.findByText(DouyinElements.SEARCH_BUTTON_TEXT, exact = false, timeout = 3000)
        
        // 如果找不到，尝试通过描述查找
        if (node == null) {
            node = locator.findByDescription(DouyinElements.SEARCH_ICON_DESC, exact = false, timeout = 3000)
        }
        
        if (node == null) {
            Log.e(TAG, "未找到搜索按钮")
            // 打印节点树用于调试
            locator.printCurrentTree()
            return false
        }
        
        return AccessibilityHelper.clickNode(node)
    }
    
    /**
     * 输入搜索文本
     */
    private fun inputSearchText(text: String): Boolean {
        // 查找输入框
        val editTexts = locator.findByClassName(DouyinElements.EDIT_TEXT_CLASS, timeout = 3000)
        
        if (editTexts.isEmpty()) {
            Log.e(TAG, "未找到输入框")
            locator.printCurrentTree()
            return false
        }
        
        // 使用第一个可见的输入框
        val inputNode = editTexts.firstOrNull { it.isVisibleToUser }
        if (inputNode == null) {
            Log.e(TAG, "未找到可见的输入框")
            return false
        }
        
        return AccessibilityHelper.inputText(inputNode, text)
    }
    
    /**
     * 触发搜索
     */
    private fun triggerSearch(): Boolean {
        // 方法1: 查找搜索按钮
        val searchButton = locator.findByText("搜索", exact = true, timeout = 2000)
        if (searchButton != null && AccessibilityHelper.clickNode(searchButton)) {
            return true
        }
        
        // 方法2: 模拟回车键（如果支持）
        // 这里可以添加发送回车键的代码
        
        // 方法3: 等待自动搜索
        Thread.sleep(1000)
        return true
    }
    
    /**
     * 等待搜索结果加载
     */
    private fun waitForSearchResults(): Boolean {
        // 等待RecyclerView出现
        val startTime = System.currentTimeMillis()
        val timeout = 10000L
        
        while (System.currentTimeMillis() - startTime < timeout) {
            val recyclerViews = locator.findByClassName(DouyinElements.RESULT_LIST_CLASS, timeout = 1000)
            if (recyclerViews.isNotEmpty()) {
                // 检查是否有内容
                val hasContent = recyclerViews.any { it.childCount > 0 }
                if (hasContent) {
                    return true
                }
            }
            Thread.sleep(500)
        }
        
        return false
    }
    
    /**
     * 进入博主主页
     */
    suspend fun enterProfile(bloggerName: String): Boolean {
        Log.d(TAG, "👤 尝试进入博主主页: $bloggerName")
        
        // 先打印当前界面的节点树，用于调试
        Log.d(TAG, "========== 搜索结果界面节点树 ==========")
        locator.printCurrentTree()
        Log.d(TAG, "==========================================")
        
        // 策略1: 查找包含博主名称的元素
        val nodes = locator.findAllByText(bloggerName, exact = false)
        
        if (nodes.isEmpty()) {
            Log.e(TAG, "❌ 未找到博主名称: $bloggerName")
            
            // 策略2: 尝试点击第一个搜索结果
            Log.d(TAG, "尝试点击第一个搜索结果...")
            return clickFirstSearchResult()
        }
        
        Log.d(TAG, "找到 ${nodes.size} 个包含博主名称的元素")
        
        // 点击每个匹配的元素
        for ((index, node) in nodes.withIndex()) {
            Log.d(TAG, "尝试点击第 ${index + 1} 个元素")
            
            // 打印节点信息
            val text = AccessibilityHelper.getNodeText(node)
            val desc = AccessibilityHelper.getNodeDescription(node)
            val className = node.className
            Log.d(TAG, "节点信息: text=$text, desc=$desc, class=$className")
            
            if (AccessibilityHelper.clickNode(node)) {
                Log.d(TAG, "✅ 已点击元素")
                delay(3000)
                
                // 验证是否进入主页
                if (isOnProfilePage()) {
                    Log.d(TAG, "✅ 已进入博主主页")
                    return true
                } else {
                    Log.w(TAG, "⚠️ 点击后未进入主页，尝试返回")
                    goBack()
                    delay(1000)
                }
            }
        }
        
        Log.e(TAG, "❌ 无法进入博主主页")
        return false
    }
    
    /**
     * 点击第一个搜索结果
     */
    private suspend fun clickFirstSearchResult(): Boolean {
        // 查找RecyclerView
        val recyclerViews = locator.findByClassName("androidx.recyclerview.widget.RecyclerView", timeout = 2000)
        
        if (recyclerViews.isEmpty()) {
            Log.e(TAG, "❌ 未找到搜索结果列表")
            return false
        }
        
        val recyclerView = recyclerViews.first()
        Log.d(TAG, "找到搜索结果列表，子元素数量: ${recyclerView.childCount}")
        
        // 点击第一个子元素
        if (recyclerView.childCount > 0) {
            val firstItem = recyclerView.getChild(0)
            if (firstItem != null) {
                Log.d(TAG, "尝试点击第一个搜索结果")
                if (AccessibilityHelper.clickNode(firstItem)) {
                    Log.d(TAG, "✅ 已点击第一个搜索结果")
                    delay(3000)
                    
                    if (isOnProfilePage()) {
                        Log.d(TAG, "✅ 已进入博主主页")
                        return true
                    }
                }
            }
        }
        
        return false
    }
    
    /**
     * 检查是否在博主主页
     */
    fun isOnProfilePage(): Boolean {
        // 检查是否有"作品"、"喜欢"等标签
        val indicators = listOf(
            DouyinElements.PROFILE_TAB_WORKS,
            DouyinElements.PROFILE_TAB_LIKE,
            DouyinElements.PROFILE_TAB_DYNAMIC
        )
        
        for (indicator in indicators) {
            val node = locator.findByText(indicator, exact = true, timeout = 2000)
            if (node != null) {
                return true
            }
        }
        
        return false
    }
    
    /**
     * 返回上一页
     */
    fun goBack(): Boolean {
        return service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
    }
    
    /**
     * 返回主页
     */
    fun goHome(): Boolean {
        return service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
    }
}
