package com.douyin.automation.task

import android.util.Log
import com.douyin.automation.douyin.DouyinLauncher
import com.douyin.automation.douyin.DouyinNavigator
import com.douyin.automation.logger.OperationLogger
import com.douyin.automation.network.WebSocketClient
import kotlinx.coroutines.delay

/**
 * 搜索并探索任务
 */
class SearchAndExploreTask(
    private val launcher: DouyinLauncher,
    private val navigator: DouyinNavigator,
    private val logger: OperationLogger,
    private val wsClient: WebSocketClient? = null
) {

    companion object {
        private const val TAG = "SearchAndExploreTask"
    }

    private var isCancelled = false
    private val reporter = TaskReporter(wsClient, logger)

    suspend fun execute(taskId: String, keyword: String, scrollCount: Int = 3): Boolean {
        try {
            logger.clear()
            logger.log("开始", "搜索并探索: $keyword", true)

            if (isCancelled) {
                reportCancelled(taskId, keyword)
                return false
            }

            if (!launchDouyinApp()) {
                reportFailure(taskId, keyword, "启动抖音失败")
                return false
            }

            if (isCancelled) {
                reportCancelled(taskId, keyword)
                return false
            }

            if (!searchKeyword(taskId, keyword)) {
                reportFailure(taskId, keyword, "搜索关键词失败")
                return false
            }

            if (isCancelled) {
                reportCancelled(taskId, keyword)
                return false
            }

            if (!enterFirstVideoAuthor(taskId)) {
                reportFailure(taskId, keyword, "进入第一个作品作者主页失败")
                return false
            }

            if (isCancelled) {
                reportCancelled(taskId, keyword)
                return false
            }

            if (!scrollProfile(taskId, scrollCount)) {
                reportFailure(taskId, keyword, "主页滑动失败")
                return false
            }

            if (isCancelled) {
                reportCancelled(taskId, keyword)
                return false
            }

            if (!enterFirstVideo(taskId)) {
                reportFailure(taskId, keyword, "进入第一个作品失败")
                return false
            }

            if (isCancelled) {
                reportCancelled(taskId, keyword)
                return false
            }

            if (!returnToApp(taskId)) {
                reportFailure(taskId, keyword, "返回应用主页失败")
                return false
            }

            reportSuccess(taskId, keyword)
            logger.log("完成", "任务成功", true)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "任务执行异常", e)
            logger.log("异常", "执行失败", false, e.message)
            reportFailure(taskId, keyword, e.message ?: "未知错误")
            return false
        }
    }

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

    private suspend fun searchKeyword(taskId: String, keyword: String): Boolean {
        logger.log("搜索关键词", "开始搜索", true)
        val success = navigator.searchKeyword(keyword)
        reporter.reportStepResult(taskId, 1, success, if (success) "搜索完成" else "搜索失败")

        if (!success) {
            logger.log("搜索关键词", "失败", false, "搜索操作失败")
            return false
        }

        logger.log("搜索关键词", "成功", true)
        return true
    }

    private suspend fun enterFirstVideoAuthor(taskId: String): Boolean {
        logger.log("进入作者主页", "点击首个作者", true)
        val success = navigator.enterFirstVideoAuthor()
        reporter.reportStepResult(taskId, 2, success, if (success) "已进入第一个作品作者主页" else "进入第一个作品作者主页失败")

        if (!success) {
            logger.log("进入作者主页", "失败", false, "无法进入作者主页")
            return false
        }

        logger.log("进入作者主页", "成功", true)
        return true
    }

    private suspend fun scrollProfile(taskId: String, scrollCount: Int): Boolean {
        logger.log("滑动主页", "开始滑动", true)
        val success = navigator.scrollProfile(scrollCount)
        reporter.reportStepResult(taskId, 3, success, if (success) "主页滑动完成" else "主页滑动失败")

        if (!success) {
            logger.log("滑动主页", "失败", false, "主页滑动失败")
            return false
        }

        logger.log("滑动主页", "成功", true)
        return true
    }

    private suspend fun enterFirstVideo(taskId: String): Boolean {
        logger.log("进入作品", "打开第一个作品", true)
        val success = navigator.enterFirstVideo()
        reporter.reportStepResult(taskId, 4, success, if (success) "已进入第一个作品" else "进入第一个作品失败")

        if (!success) {
            logger.log("进入作品", "失败", false, "无法进入第一个作品")
            return false
        }

        logger.log("进入作品", "成功", true)
        return true
    }

    private suspend fun returnToApp(taskId: String): Boolean {
        logger.log("返回首页", "执行返回", true)
        val success = navigator.returnToAppHome()
        reporter.reportStepResult(taskId, 5, success, if (success) "已返回App主界面" else "返回App主界面失败")

        if (!success) {
            logger.log("返回首页", "失败", false, "无法返回应用主页")
            return false
        }

        logger.log("返回首页", "成功", true)
        return true
    }

    private fun reportSuccess(taskId: String, keyword: String) {
        val result = reporter.buildTaskResultPayload(
            taskType = "search_and_explore",
            target = keyword,
            success = true
        )
        reporter.reportTaskResult(taskId, true, result)
    }

    private fun reportFailure(taskId: String, keyword: String, error: String) {
        val result = reporter.buildTaskResultPayload(
            taskType = "search_and_explore",
            target = keyword,
            success = false,
            error = error
        )
        reporter.reportTaskResult(taskId, false, result)
    }

    private fun reportCancelled(taskId: String, keyword: String) {
        logger.log("取消", "任务已取消", true)
        reportFailure(taskId, keyword, "任务已取消")
    }

    fun cancel() {
        isCancelled = true
        logger.log("取消", "任务已取消", true)
    }
}
