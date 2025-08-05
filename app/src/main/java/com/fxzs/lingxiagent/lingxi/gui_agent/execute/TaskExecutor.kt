package com.skythinker.gui_agent.execute

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import cn.vove7.andro_accessibility_api.utils.ScreenshotUtils
import cn.vove7.auto.core.AutoApi
import cn.vove7.auto.core.api.containsText
import cn.vove7.auto.core.api.editor
import cn.vove7.auto.core.api.longClick
import cn.vove7.auto.core.api.pressOn
import cn.vove7.auto.core.api.swipe
import cn.vove7.auto.core.api.swipeDirection
import com.example.service_api.data.PredictionAction
import com.skythinker.gui_agent.util.ClipboardUtils
import com.skythinker.gui_agent.entity.ActionType
import com.skythinker.gui_agent.entity.ExecuteStatus
import com.skythinker.gui_agent.entity.OperatorType
import com.skythinker.gui_agent.entity.ScrollDirection
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import kotlin.random.Random

object TaskExecutor {

    private var tag : String = "TaskExecutor"
    private var breakFlag = false
    private var query = ""
    private var appInstalled: Boolean = false
    private var message = ""


    @RequiresApi(Build.VERSION_CODES.R)
    suspend fun execute(context: Activity, action: PredictionAction, callback: (Int, String, String, Boolean, Boolean) -> Unit) {
        val app = context.applicationContext as? Application
            ?: throw IllegalStateException("Expected application context")

        val screenSize = ScreenshotUtils.getScreenSizeWithNavigationBar(context)
        Timber.Forest.tag(tag).i("execute action %s", action.toString())
        when (action.action_type) {
            ActionType.PRESS.alias -> {
                when (action.target) {
                    OperatorType.HOME.keyEvent -> AutoApi.Companion.home()
                    OperatorType.BACK.keyEvent -> AutoApi.Companion.back()
                    OperatorType.MENU.keyEvent -> {

                    }
                    OperatorType.ENTER.keyEvent -> {

                    }
                    OperatorType.APPSELECT.keyEvent -> {

                    }
                    OperatorType.CLEAR.keyEvent -> {

                    }
                    OperatorType.POWER.keyEvent -> {

                    }
                    OperatorType.VOLUME_UP.keyEvent -> {

                    }
                    OperatorType.VOLUME_DOWN.keyEvent -> {

                    }
                    OperatorType.VOLUME_MUTE.keyEvent -> {

                    }
                    else -> Timber.Forest.tag(tag).i("未知的操作类型PRESS：${action.target}")
                }
            }
            ActionType.CLICK.alias -> {
                val x = (action.from[0] * screenSize.x).toInt()
                val y = (action.from[1] * screenSize.y).toInt()
                // 这里把单个点改成点阵，按5像素点来获取，并随机调整x,y的值上下不超过5
                val randomX = Random.nextInt(-2, 4) // 生成-2到3之间的随机数
                val randomY = Random.nextInt(-2, 4) // 生成-2到3之间的随机数
                val adjustedPoints = listOf(
                    Pair(x, y),
                    Pair(x + randomX, y + randomY),
                    Pair(x + randomX + 1, y + randomY + 1),
                    Pair(x + randomX - 1, y + randomY - 1),
                    Pair(x + randomX + 2, y + randomY + 2),
                    Pair(x + randomX - 2, y + randomY - 2)
                )
//                adjustedPoints.forEachIndexed { index, (adjustedX, adjustedY) ->
//                    Timber.Forest.tag(tag).i("点击坐标 ${index + 1}：$adjustedX, $adjustedY")
//                }
                val isclick = pressOn(convertToAndroidPairList(adjustedPoints))
                Timber.Forest.tag(tag).i("点击坐标：$x   $y 返回结果：$isclick")
            }
            ActionType.INPUT.alias -> {
                runBlocking {
                    action.text?.let {
                        Timber.Forest.tag(tag).i("输入文本：$it")
                        try {
                            editor().apply {
                                text = it
                            }
                            // 尝试查找并点击搜索按钮
                            findAndClickSearchButton("搜索")
                        } catch (e: Exception) {
                            Timber.Forest.tag(tag).i("输入文本异常：$e")
                            //设置文本到粘贴板，这里无法获取到输入控件，走大模型模拟点击方案
                            ClipboardUtils.copyText(context, it)
                            val positionString = it.firstOrNull() ?: ""
                            query = "帮我点击一下词语$it 的“ $positionString ”"
                            callback(ExecuteStatus.INPUT.alias, query, "", false, false)
                            query = ""
                        }
                    }
                }
            }
            ActionType.LONG_PRESS.alias -> {
                val x = (action.from[0] * screenSize.x).toInt()
                val y = (action.from[1] * screenSize.y).toInt()
                longClick(x, y)
            }
            ActionType.OPEN_APP.alias -> {
                if(findApp(action,app)){
//                    todo这里需要增加一个deeplink方式打开app


//                    val url = curTaskItem.deeplinkUrl
//                    val args = curTaskItem.deeplinkUrlArgs
//
//                    val defaultValue = curTaskItem.slotDefaultValue
//                    val slotsList = curTaskItem.slotKeyList.split(',')
//                    val value = slotsList.find { slot -> slots[slot] != null }
//                    val resultText = value?.let { slots[it] } ?: defaultValue
//
//                    val fullUrl = "${url}?${args}${resultText}"
//
//                    runBlocking {
//                        try {
//                            val intent = Intent(Intent.ACTION_VIEW)
//                            intent.setData(Uri.parse(fullUrl))
//                            context.startActivity(intent)
//                        } catch (e: Exception){
//                            println(e)
//                        }
//                    }

                    appInstalled = true
                    openThirdPartyApp(context, action.package_name.split("/")[0], action.package_name.split("/")[1])
                    //这里是为了截图更准确，延迟500ms执行截图操作，避免截图时操作还未完成
                    delay(500)
                }else{
                    message = if (action.app_name == "App之外"){
                        "当前app暂不支持操作"
                    }else{
                        "要执行的程序（${action.app_name}）还未安装，请到应用商店下载安装！"
                    }
//                    BroadcastUtils.completeBroadcast()
//                    closeMenu()
                    //打断执行
                    breakFlag = true
                    //app安装状态
                    appInstalled = false
                    callback(ExecuteStatus.OPEN_APP_FAIL.alias,query,message,appInstalled,breakFlag)
                    message = ""
                }
            }
            ActionType.SCROLL.alias -> {
                val x = (action.from[0] * screenSize.x).toInt()
                val y = (action.from[1] * screenSize.y).toInt()
                when (action.direction) {
                    ScrollDirection.UP.alias -> {
                        swipeDirection(
                            x,
                            y,
                            -1,
                            800,
                            400
                        )
                    }
                    ScrollDirection.DOWN.alias -> {
                        swipeDirection(
                            x,
                            y,
                            1,
                            800,
                            400
                        )
                    }
                    ScrollDirection.LEFT.alias -> {
                        swipeDirection(
                            x,
                            y,
                            -2,
                            800,
                            400
                        )
                    }
                    ScrollDirection.RIGHT.alias -> {
                        swipeDirection(
                            x,
                            y,
                            2,
                            800,
                            400
                        )
                    }
                    else -> {
                        Timber.Forest.tag(tag).i("未知的操作类型SCROLL：${action.target}")
                    }

                }

            }
            ActionType.DRAG.alias -> {
                val x1 = (action.from[0] * screenSize.x).toInt()
                val y1 = (action.from[1] * screenSize.y).toInt()
                val x2 = (action.to[0] * screenSize.x).toInt()
                val y2 = (action.to[1] * screenSize.y).toInt()
                swipe(x1, y1, x2, y2, 400)
            }
            // ... 其他 action_type 分支（省略以实现简洁）
            else -> Timber.Forest.tag(tag).i("未知的操作类型action_type：${action.action_type}")
        }
    }

    private fun findAndClickSearchButton(buttonText: String) {
        try {
            // 尝试查找搜索按钮并点击
            containsText(buttonText).let {
                Timber.Forest.tag(tag).i("找到搜索按钮并点击")
                it.click()
            }
        }catch (e: Exception) {
            Timber.tag(tag).i("查找并点击搜索按钮异常：$e")
        }
    }

    private fun findApp(action: PredictionAction, app: Application): Boolean {
        /**
         * 验证关联app是否安装
         */
//        var curAppName: String? = null // 应用名称，初始化为null
        var curPackageName: String? = null // 应用包名，初始化为null
        if(action.package_name.isNullOrEmpty().not()){
//            curAppName = action.app_name
            curPackageName = action.package_name.split("/")[0].toString()
        }
        val packageManager: PackageManager = app.packageManager
        if(curPackageName.isNullOrEmpty()){
            return false
        }
        return try {
            packageManager.getPackageInfo(curPackageName, 0)
            true
        } catch (e : PackageManager.NameNotFoundException) {
            false
        }
    }

    fun openThirdPartyApp(context: Context, packageName: String, activityClassName: String) {
        try{
//            val intent = Intent()
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//            intent.setComponent(ComponentName(packageName, activityClassName))
//            context.startActivity(intent)

            val packageManager: PackageManager = context.packageManager
            val intent: Intent? = packageManager.getLaunchIntentForPackage(packageName)

            if (intent != null) {
                // 添加 FLAG_ACTIVITY_NEW_TASK 确保在新任务中启动
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                context.startActivity(intent)
                Timber.tag(tag).i("成功启动应用: $packageName")
            } else {
                Timber.tag(tag).i("未找到包名为 $packageName 的启动Activity，或者应用未安装。")
            }

        }catch (e:Exception){
            Timber.Forest.tag("HandlerLineTaskLlm").i("启动三方app异常：$e")
        }

    }

    // 将kotlin.Pair列表转换为android.util.Pair列表
    fun convertToAndroidPairList(kotlinList: List<Pair<Int, Int>>): List<android.util.Pair<Int, Int>> {
        return kotlinList.map { android.util.Pair(it.first, it.second) }
    }



}