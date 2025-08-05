package com.fxzs.lingxiagent.actions

import android.app.Activity
import android.provider.Settings
import cn.vove7.andro_accessibility_api.AccessibilityApi.Companion.isBaseServiceEnable
import com.fxzs.lingxiagent.helper.FloatHelper
import com.fxzs.lingxiagent.lingxi.config.PermissionType
import com.fxzs.lingxiagent.lingxi.main.utils.BroadcastUtils
import com.fxzs.lingxiagent.lingxi.main.utils.BroadcastUtils.replyMessageBroadcast
import com.fxzs.lingxiagent.lingxi.main.utils.BroadcastUtils.replyPermissionCardBroadcast
import com.skythinker.gui_agent.UiAgentManager
import com.skythinker.gui_agent.entity.TaskStatus
import timber.log.Timber

object HandlerLlm {

    private var Tag : String = "HandlerLlm"

    fun start(context: Activity, query: String){
        Timber.tag(Tag).i("HandlerLlm start")

//            HandlerLineTask.INSTANCE.start(context, AsrRespStr);
        if (!Settings.canDrawOverlays(context)) {
            replyPermissionCardBroadcast(PermissionType.FLOAT)
            return
        }
        if (!isBaseServiceEnable) {
            replyPermissionCardBroadcast(PermissionType.ACCESSIBILITY)
            return
        }
        //打开按钮
        openMenu()

        UiAgentManager.startExecutor(context, query) { code,message ->
            Timber.tag(Tag).i("code : $code  message : $message")
            when(code){
                TaskStatus.SUCCESS.alias -> {
                    closeMenu()
                    BroadcastUtils.completeBroadcast()
                    replyMessageBroadcast(message,true,false)
                }
                //截图失败
                TaskStatus.SCREEN_SHOT_ERROR.alias -> {

                }
                TaskStatus.OPEN_APP.alias -> {

                }
                //网络错误
                TaskStatus.NETWORK_ERROR.alias -> {
                    closeMenu()
                    replyMessageBroadcast(message,true,false)
                    BroadcastUtils.completeBroadcast()
                }
                //多轮会话
                TaskStatus.MULTIPLE_CONVERSATION.alias -> {

                }
                TaskStatus.MULTIPLE_OPERATIONS.alias -> {
                    replyMessageBroadcast(message,true,false)
                }
                TaskStatus.OPEN_MENU.alias -> {
                    openMenu()
                }
                TaskStatus.CLOSE_MENU.alias -> {
                    closeMenu()
                }
                TaskStatus.EXCEPTION.alias -> {
                    closeMenu()
                    replyMessageBroadcast(message,true,false)
                    BroadcastUtils.completeBroadcast()
                }
                TaskStatus.TASK_EXECUTE_FAIL.alias -> {
                    closeMenu()
                    replyMessageBroadcast(message,true,false)
                    BroadcastUtils.completeBroadcast()
                }
                TaskStatus.TASK_INTERRUPTION.alias -> {
                    closeMenu()
                    replyMessageBroadcast(message,true,false)
                    BroadcastUtils.completeBroadcast()
                }
                else -> {
                    Timber.Forest.tag(Tag).i("未知的CODE：${code}")
                }
            }
        }
    }

    private fun openMenu() {
        Timber.tag(Tag).i("openMenu 打开悬浮窗菜单")

        FloatHelper.openFloatMenu(close = { close() })
    }
    private fun closeMenu() {

        Timber.tag(Tag).i("closeMenu 关闭悬浮窗菜单")
        FloatHelper.closeFloatMenu()
    }

    fun close() {
        Timber.tag(Tag).i("close()")
        UiAgentManager.breakTask()
        closeMenu()
    }

}