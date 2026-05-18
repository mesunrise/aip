package com.douyin.automation.task

import android.util.Log
import com.douyin.automation.douyin.DouyinLauncher
import com.douyin.automation.douyin.DouyinNavigator
import com.douyin.automation.logger.OperationLogger
import com.douyin.automation.network.WebSocketClient
import kotlinx.coroutines.delay

/**
 * 搜索博主任务
 */
class SearchBloggerTask(
    private val launcher: DouyinLauncher,
    private val navigator: DouyinNavigator,
    private val logger: OperationLogger,
    private val wsClient: WebSocketClient? = null
) {

    companion object {
        private const val TAG = "SearchBloggerTask"
    }

    private var isCancelled = false
    private val reporter = TaskReporter(wsClient, logger)

    /**
     * 执行任务
     */
    suspend fun execute(taskId: String, bloggerName: String): Boolean {
        try {
            logger.clear()
            logger.log("开始", "搜索博主: $bloggerName", true)

            if (isCancelled) {
                reportCancelled(taskId, bloggerName)
                return false
            }
            if (!launchDouyinApp()) {
                reportFailure(taskId, bloggerName, "启动抖音失败")
                return false
            }

            if (isCancelled) {
                reportCancelled(taskId, bloggerName)
                return false
            }
            if (!searchBlogger(bloggerName)) {
                reportFailure(taskId, bloggerName, "搜索博主失败")
                return false
            }

            if (isCancelled) {
                reportCancelled(taskId, bloggerName)
                return false
            }
            if (!enterProfile(bloggerName)) {
                reportFailure(taskId, bloggerName, "进入博主主页失败")
                return false
            }

            if (isCancelled) {
                reportCancelled(taskId, bloggerName)
                return false
            }

            reportSuccess(taskId, bloggerName)
            logger.log("完成", "任务成功", true)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "任务执行异常", e)
            logger.log("异常", "执行失败", false, e.message)
            reportFailure(taskId, bloggerName, e.message ?: "未知错误")
            return false
        }
    }

    /**
     * 启动抖音
     */
    private suspend fun launchDouyinApp(): Boolean {
        logger.log("启动抖音", "检查安装", true)

        if (!launcher.isDouyinInstalled()) {
            logger.log("启动抖音", "失败", false, "抖音未安装")
            return false
        }

        logger.log("启动抖音", "启动应用", true)

        if (!launcher.launchDouyin()) {
            logger.log("启动抖音", "失败", false, "无法启动抖音")
            return false
        }

        logger.log("启动抖音", "成功", true)
        delay(3000)
        return true
    }

    /**
     * 搜索博主
     */
    private suspend fun searchBlogger(bloggerName: String): Boolean {
        logger.log("搜索博主", "开始搜索", true)

        if (!navigator.searchBlogger(bloggerName)) {
            logger.log("搜索博主", "失败", false, "搜索操作失败")
            return false
        }

        logger.log("搜索博主", "成功", true)
        return true
    }

    /**
     * 进入主页
     */
    private suspend fun enterProfile(bloggerName: String): Boolean {
        logger.log("进入主页", "查找博主", true)

        if (!navigator.enterProfile(bloggerName)) {
            logger.log("进入主页", "失败", false, "无法进入主页")
            return false
        }

        logger.log("进入主页", "成功", true)
        return true
    }

    private fun reportSuccess(taskId: String, bloggerName: String) {
        val result = reporter.buildTaskResultPayload(
            taskType = "search_blogger",
            target = bloggerName,
            success = true
        )
        reporter.reportTaskResult(taskId, true, result)
    }

    private fun reportFailure(taskId: String, bloggerName: String, error: String) {
        val result = reporter.buildTaskResultPayload(
            taskType = "search_blogger",
            target = bloggerName,
            success = false,
            error = error
        )
        reporter.reportTaskResult(taskId, false, result)
    }

    private fun reportCancelled(taskId: String, bloggerName: String) {
        logger.log("取消", "任务已取消", true)
        reportFailure(taskId, bloggerName, "任务已取消")
    }

    /**
     * 取消任务
     */
    fun cancel() {
        isCancelled = true
        logger.log("取消", "任务已取消", true)
    }
}
