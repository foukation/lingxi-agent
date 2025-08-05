package com.ai.multimodal.utils.eventTracker

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.cmdc.ai.assist.eventTracking.AIEventTracker
import com.cmdc.ai.assist.eventTracking.BuildConfig
import com.cmdc.ai.assist.eventTracking.EventData
import com.cmdc.ai.assist.eventTracking.EventTrackerConfig
import com.fxzs.lingxiagent.lingxi.main.utils.DeviceUUIDGenerator
import org.json.JSONObject

/**
 *
 * 参数名	必选	类型	说明
 * im	是	string	用户标识(imei,sn)
 * av	是	string	应用版本号
 * un	否	string	上传网络(5G,4G,WIFI)
 * ut	是	string	上传时间(yyyy-MM-dd hh:mm:dd)
 * sv	是	string	sdk版本
 * mac	否	string	mac地址
 * os	是	string	操作系统（android,ios）
 * et	是	string	行为时间
 * ei	是	string	事件ID
 * el	是	string	事件名称（event lable）
 * co	是	string	厂商名称
 * mo	是	string	设备型号
 * ch	否	string	渠道
 * em	是	string	其他事件参数（json格式）
 *
 *
 * 事件跟踪管理器
 *
 * 负责管理应用的事件跟踪功能，包括初始化跟踪器、启动和关闭应用事件的记录和上传等。
 * 采用单例模式设计，确保在整个应用生命周期中只有一个实例。
 *
 * 主要功能包括：
 * 1. 初始化事件跟踪服务
 * 2. 记录和上传应用启动事件
 * 3. 记录和上传应用关闭事件
 */
class EventTrackerManager private constructor() {

    companion object {
        @Volatile
        var INSTANCE: EventTrackerManager? = null
        private val TAG = EventTrackerManager::class.simpleName.toString()

        /**
         * 获取 EventTrackerManager 的单例实例
         *
         * 使用双重检查锁定模式实现线程安全的单例模式。
         * 首先检查实例是否已经创建，如果未创建则进入同步块再次检查并创建实例。
         *
         * @return EventTrackerManager的单例实例
         */
        private fun getInstance(): EventTrackerManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: EventTrackerManager().also { INSTANCE = it }
            }
        }

        /**
         * 初始化 EventTrackerManager
         *
         * 通过获取单例实例并调用其内部初始化方法来完成EventTrackerManager的初始化。
         *
         * @param context 应用上下文，用于初始化相关服务
         */
        fun initialize(context: Context) {
            getInstance().init(context)
        }
    }

    // 应用上下文
    private lateinit var applicationContext: Context

    // 初始化状态
    private var isInitialized = false

    private lateinit var tracker: AIEventTracker

    /**
     * 内部初始化方法
     */
    private fun init(context: Context) {
        if (isInitialized) {
            return
        }

        this.applicationContext = context.applicationContext
        this.isInitialized = true

        initializeServices()
    }

    private fun initializeServices() {

        val config = EventTrackerConfig(
            maxEventsPerBatch = 20,
            processIntervalMillis = 10000,
            autoUpload = true,
            debugMode = BuildConfig.DEBUG
        )

        tracker = AIEventTracker.getInstance(applicationContext)
        tracker.init(config)

    }

    fun startApp() {
        var versionName = ""
        try {
            val packageInfo: PackageInfo = applicationContext.getPackageManager()
                .getPackageInfo(applicationContext.getPackageName(), 0)

            versionName = packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        val json = JSONObject()
        json.put("pkg", applicationContext.packageName)
        json.put("ver", versionName)
        tracker.processAndUploadEvents(
            EventData(
                im = DeviceUUIDGenerator.getDeviceUUID(applicationContext),
                av = versionName,
                un = "wifi",
                sv = "1.0.0",
                mac = "",
                ei = "100001",
                el = "start app",
                co = Build.MANUFACTURER,
                mo = Build.MODEL,
                ch = "",
                em = json.toString()
            )
        )

    }

    fun closeApp() {

        tracker.processAndUploadEvents(
            EventData(
                im = DeviceUUIDGenerator.getDeviceUUID(applicationContext),
                av = "1.0.0",
                un = "wifi",
                sv = "1.0.0",
                mac = "xxx",
                ei = "100002",
                el = "close app",
                co = "xxx",
                mo = "xxx",
                ch = "xxx",
                em = "{}"
            )
        )

    }

}