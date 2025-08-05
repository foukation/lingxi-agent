package com.skythinker.gui_agent.actions

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import cn.vove7.andro_accessibility_api.utils.ScreenshotUtils
import com.example.common.dialog.CustomPopupWindow
import com.example.service_api.IntentionApi
import com.example.service_api.data.LLmQueryParams
import com.example.service_api.data.LlmQueryResult
import com.skythinker.gui_agent.entity.ExecuteStatus
import com.skythinker.gui_agent.execute.TaskExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

object HandlerLineTaskLlm {

    private var requestIsRuntime: Boolean = false
    private var sessionID: String = ""
    private var Tag : String = "HandlerLineTaskLlm"
    private var breakFlag = false
    // 假设的最大递归尝试次数
    private const val MAX_RETRY_COUNT = 10
    private var query: String = ""
    private var targetQuery: String = ""
    private var AppInstalled: Boolean = true
    private var appMessage: String = ""


    @RequiresApi(Build.VERSION_CODES.R)
    fun getAndUploadScreenshot(
        context: Activity,
        sessionID: String,
        queryString: String,//query，二次交互时，会修改
        targetQueryString: String,//目标query，保持不变
        retryCount: Int = 0,
        isNewTask: Boolean = true,
        callback: (String) -> Unit
    ) {
        query = targetQueryString
        Timber.tag(Tag).i("queryString $queryString ")
        targetQuery = targetQueryString
        if (breakFlag) {
            Timber.tag(Tag).i("任务中断 AppInstalled $AppInstalled")
            breakFlag = false
//            requestIsRuntime = false
            if (AppInstalled){
                callback("任务已中断")

            } else {
                Timber.tag(Tag).i(appMessage)
                callback(appMessage)
            }
            return
        }

        if (isNewTask) {
            handleLlmAction(
                context, sessionID, null, queryString, retryCount, callback
            )
        } else {
            val screenshotStartTime = System.currentTimeMillis() // 记录截图开始时间
            Timber.tag(Tag).i("截图开始时间: $screenshotStartTime")
            ScreenshotUtils.getScreenshotBase64(context) { base64String ->
                val screenshotEndTime = System.currentTimeMillis() // 记录截图结束时间
                Timber.tag(Tag).i("截图时间: ${screenshotEndTime - screenshotStartTime} ms")
                if (base64String != null) {
                    handleLlmAction(
                        context, sessionID, base64String, queryString, retryCount, callback
                    )
                } else {
//                    requestIsRuntime = false
                    if (retryCount < MAX_RETRY_COUNT) {

                        // 若未达到最大尝试次数，则递归调用自身重新截图
                        Timber.tag(Tag)
                            .i("截图失败，重新尝试（尝试次数：${retryCount + 1}/${MAX_RETRY_COUNT}）")
                        getAndUploadScreenshot(
                            context,
                            sessionID,
                            query,
                            targetQuery,
                            retryCount + 1,
                            false,
                            callback
                        )
                    } else {
                        // 若已达到最大尝试次数，则通知调用者截图失败
                        Timber.tag(Tag).i("截图多次尝试均失败")
                        callback("截图多次尝试均失败")

                    }
                }
            }
        }

    }
    @RequiresApi(Build.VERSION_CODES.R)
    fun handleLlmAction(
        context: Activity,
        sessionID: String,
        base64String: String?,
        resultString: String,
        retryCount: Int,
        callback: (String) -> Unit
    ) {
        try {
            val params = LLmQueryParams(sessionID, base64String, resultString)
            Timber.tag(Tag).i(" 请求params sessionID: $sessionID $resultString")
            val uploadStartTime = System.currentTimeMillis() // 记录上传开始时间
            IntentionApi.handlerLlmAction(
                params,
                onSuccess = @RequiresApi(Build.VERSION_CODES.R) { resp: LlmQueryResult ->
                    Timber.tag(Tag).i("resp : %s", resp.toString())
                    if (resp.success) {
                        val uploadEndTime = System.currentTimeMillis() // 假设的上传结束时间（实际应在上传逻辑完成后记录）
                        Timber.tag(Tag).i("截图上传时间: ${uploadEndTime - uploadStartTime} ms")
                        CoroutineScope(Dispatchers.Main).launch {

//                            requestIsRuntime = false
                            // 检查操作是否完成
                            resp?.data?.prediction?.action?.let { action ->
                                if (action.action_type != "finished") {
                                    // 执行当前任务
                                    try {
                                        TaskExecutor.execute(context, resp.data.prediction.action){
                                                code, query1, message, appinstalled, breakflag ->
                                            when(code){
                                                ExecuteStatus.INPUT.alias -> {
                                                    query = query1
                                                    Timber.tag(Tag).i("操作完成 callback... $query")
                                                }
                                                ExecuteStatus.OPEN_APP_FAIL.alias -> {
                                                    Timber.tag(Tag).i("操作完成 callback... $message")
                                                    AppInstalled = appinstalled
                                                    breakFlag = breakflag
                                                    appMessage = message
                                                }
                                            }
                                        }
                                    }catch (e: Exception) {
                                        e.printStackTrace()
//                                        callback("执行任务出错")
                                        Timber.tag(Tag).e("执行异常... ${e.message} ${e.printStackTrace()}")
                                        Timber.tag(Tag)
                                            .i("执行异常，重新尝试（尝试次数：${retryCount + 1}/${MAX_RETRY_COUNT}")
                                        if (retryCount < MAX_RETRY_COUNT) {
                                            // 重试逻辑（注意：此处可能需要调整重试时的参数和逻辑）
                                            getAndUploadScreenshot(
                                                context,
                                                sessionID,
                                                resultString,
                                                targetQuery,
                                                retryCount + 1,
                                                false,
                                                callback
                                            )
                                        } else {
                                            // 达到最大重试次数，回调错误
                                            callback("执行异常多次尝试均失败")
                                        }
                                        return@launch
                                    }

                                    // 延迟后继续递归请求
                                    delay(2000L)
                                    // 操作未完成，递归调用处理函数
                                    Timber.tag(Tag).i("操作完成，准备递归调用...")
                                    getAndUploadScreenshot(context, sessionID, query, targetQuery, 0, false, callback)
                                } else {
                                    var text = resp?.data?.prediction?.action?.text
                                   /* CommonDialog(context)
                                        .setAutoDismiss(5000)
                                        .setText(text.toString())
//                                        .setText("iphone15有2个颜色，需要买什么颜色的呢？")
                                        .hideButtons()
//                                        .setButtonTexts("确定")
                                        .setCancelable(false)
                                        .hideEditText()
//                                        .setEditText("请输入",true)
                                        .setCallback { inputText ->
                                            // 处理用户输入
                                            query = inputText
                                            Timber.tag(Tag).i("用户输入：$inputText")
                                        }
                                        .openCommonDialog()*/
                                    CustomPopupWindow(context)
                                        .setAutoDismiss(5000)
                                        .setText(text.toString())
                                        .hideEditText()
//                                        .showEditText("请输入反馈")
                                        .hideButtons()
//                                        .setButtonTexts("提交", "取消")
                                        .setCancelable(true)
                                        .setCallback { input ->
                                            // 处理用户输入
                                            Timber.tag(Tag).i("用户输入：$input")
                                        }
                                        .show()

                                    // 操作已完成，通知调用者
                                    callback("任务完成")
                                }
                            } ?: run {
                                //返回数据无效，重新截图上传
                                Timber.tag(Tag).i("返回数据无效，重新截图上传...")
                                getAndUploadScreenshot(
                                    context,
                                    sessionID,
                                    resultString,
                                    targetQuery,
                                    0,
                                    false,
                                    callback
                                )
                            }
                        }
                    } else {
                        HandlerLineTaskLlm.sessionID = ""
                        callback("返回错误数据，任务执行失败")

//                        requestIsRuntime = false
                    }
                },
                onError = { errMsg: String ->
                    // 处理API调用错误
//                    callback("请求错误")
//                    Timber.tag(Tag).i("请求错误 $errMsg")
//                    HandlerLineTaskLlm.sessionID = ""
//                    requestIsRuntime = false
                    // 根据需求，可能还需要处理重试逻辑或直接回调错误
                    Timber.tag(Tag)
                        .i("请求错误，重新尝试（尝试次数：${retryCount + 1}/${MAX_RETRY_COUNT}）errMsg: $errMsg")
                    if (retryCount < MAX_RETRY_COUNT) {
                        // 重试逻辑（注意：此处可能需要调整重试时的参数和逻辑）
                        getAndUploadScreenshot(
                            context,
                            sessionID,
                            resultString,
                            targetQuery,
                            retryCount + 1,
                            false,
                            callback
                        )
                    } else {
                        // 达到最大重试次数，回调错误
                        callback("截图上传多次尝试均失败")
                    }
                }
            )
        }catch (e: Exception) {
//            requestIsRuntime = false

            Timber.tag(Tag).i("handleLlmAction执行异常：${e.message}")
            callback("网络请求异常")
        }

    }

    fun breakTask() {
        breakFlag = true
    }
    fun closeMenu() {

    }

}