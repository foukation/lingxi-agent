package com.skythinker.gui_agent

import android.app.Activity
import android.os.Build
import com.skythinker.gui_agent.util.SessionUtils
import com.skythinker.gui_agent.actions.HandlerLineTaskLlm
import com.skythinker.gui_agent.actions.HandlerLineTaskLlm.getAndUploadScreenshot
import com.skythinker.gui_agent.entity.TaskStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

object UiAgentManager {

    private var multipleOperation: Boolean = false
    private var sessionID: String = ""
    private var Tag : String = "UiAgentManager"
    private var query: String = ""
    private var targetQuery: String = ""
    private var AppInstalled: Boolean = false



    fun startExecutor(context: Activity, resultString: String, callback: (Int,String) -> Unit){
        query = resultString
        targetQuery = resultString
        if (multipleOperation) {
            // 已经在执行任务
            callback(TaskStatus.MULTIPLE_OPERATIONS.alias,"请勿频繁操作")
            return
        }

        //每次任务执行将app安装状态改成false
        AppInstalled = false
        multipleOperation = true
        sessionID = SessionUtils.generateSessionId()
        try{
            CoroutineScope(Dispatchers.Main).launch {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    getAndUploadScreenshot(
                        context,
                        sessionID,
                        query,
                        targetQuery
                    ) { message ->

                        Timber.tag(Tag).i(message)
                        // 任务执行完成，回调结果
                        taskCompleted()
                        if (message?.equals("任务完成") == true) {
                            callback(TaskStatus.SUCCESS.alias,"任务执行完成")
                        } else if (message?.equals("任务已中断") == true) {
                            callback(TaskStatus.TASK_INTERRUPTION.alias,"任务已中断")
                        } else if (message?.equals("执行异常多次尝试均失败") == true) {
                            callback(TaskStatus.EXCEPTION.alias,"执行任务出错")
                        } else if (message?.equals("返回错误数据，任务执行失败") == true) {
                            callback(TaskStatus.EXCEPTION.alias,"返回错误数据，任务执行失败")
                        } else if (message?.equals("请求错误") == true) {
                            callback(TaskStatus.TASK_EXECUTE_FAIL.alias,"请求错误")
                        } else if (message?.equals("截图上传多次尝试均失败") == true) {
                            callback(TaskStatus.TASK_EXECUTE_FAIL.alias,"截图上传多次尝试均失败")
                        } else if (message?.equals("网络请求异常") == true) {
                            callback(TaskStatus.TASK_EXECUTE_FAIL.alias,"网络请求异常")
                        } else if (message?.contains("当前app暂不支持操作") == true
                            || message?.contains("还未安装") == true) {
                            callback(TaskStatus.TASK_EXECUTE_FAIL.alias,message)
                        }else {
                            callback(TaskStatus.TASK_EXECUTE_FAIL.alias,"任务执行失败")
                        }
                    }
                }
            }
        }catch (e: Exception) {
            callback(TaskStatus.EXCEPTION.alias,"任务执行异常")
            multipleOperation = false
            Timber.tag(Tag).i("getAndUploadScreenshot：${e.message}")
        }
    }



    fun breakTask() {
        multipleOperation = false
        Timber.tag(Tag).i("breakTask: 任务中断")
        HandlerLineTaskLlm.breakTask()
    }

    fun taskCompleted(){
        multipleOperation = false
    }


    fun closeMenu(){
        HandlerLineTaskLlm.closeMenu()
    }


}