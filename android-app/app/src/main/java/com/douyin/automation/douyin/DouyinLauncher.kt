package com.douyin.automation.douyin

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log

/**
 * 抖音启动器
 */
class DouyinLauncher(private val context: Context) {
    
    companion object {
        private const val TAG = "DouyinLauncher"
        const val DOUYIN_PACKAGE = "com.ss.android.ugc.aweme"
    }
    
    /**
     * 检查抖音是否已安装
     */
    fun isDouyinInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo(DOUYIN_PACKAGE, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w(TAG, "抖音未安装")
            false
        }
    }
    
    /**
     * 获取抖音版本信息
     */
    fun getDouyinVersion(): String? {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(DOUYIN_PACKAGE, 0)
            packageInfo.versionName
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 启动抖音
     */
    fun launchDouyin(): Boolean {
        if (!isDouyinInstalled()) {
            Log.e(TAG, "❌ 抖音未安装")
            return false
        }
        
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(DOUYIN_PACKAGE)
            if (intent == null) {
                Log.e(TAG, "❌ 无法获取抖音启动Intent")
                return false
            }
            
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            context.startActivity(intent)
            
            Log.d(TAG, "✅ 抖音启动成功")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ 启动抖音失败", e)
            false
        }
    }
    
    /**
     * 检查抖音是否正在运行
     */
    fun isDouyinRunning(): Boolean {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? android.app.ActivityManager
            ?: return false
        
        val runningApps = am.runningAppProcesses ?: return false
        return runningApps.any { it.processName == DOUYIN_PACKAGE }
    }
    
    /**
     * 强制停止抖音
     */
    fun forceStopDouyin() {
        try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? android.app.ActivityManager
            am?.killBackgroundProcesses(DOUYIN_PACKAGE)
            Log.d(TAG, "🛑 抖音已停止")
        } catch (e: Exception) {
            Log.e(TAG, "停止抖音失败", e)
        }
    }
}
