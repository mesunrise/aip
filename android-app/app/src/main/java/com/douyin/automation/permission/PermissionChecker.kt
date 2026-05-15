package com.douyin.automation.permission

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityManager

/**
 * 权限检查工具类
 */
object PermissionChecker {
    
    /**
     * 检查无障碍服务是否已启用
     */
    fun isAccessibilityServiceEnabled(context: Context, serviceName: String): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
            ?: return false
        
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        
        if (TextUtils.isEmpty(enabledServices)) {
            return false
        }
        
        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)
        
        while (colonSplitter.hasNext()) {
            val componentName = colonSplitter.next()
            if (componentName.equals(serviceName, ignoreCase = true)) {
                return true
            }
        }
        
        return false
    }
    
    /**
     * 检查我们的无障碍服务是否已启用
     */
    fun isOurAccessibilityServiceEnabled(context: Context): Boolean {
        val serviceName = "${context.packageName}/com.douyin.automation.accessibility.AutomationAccessibilityService"
        return isAccessibilityServiceEnabled(context, serviceName)
    }
    
    /**
     * 打开无障碍设置页面
     */
    fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
    
    /**
     * 获取已启用的无障碍服务列表
     */
    fun getEnabledAccessibilityServices(context: Context): List<String> {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
            ?: return emptyList()
        
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        return enabledServices.map { it.id }
    }
}
