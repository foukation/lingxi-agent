package com.example.device_control

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.example.device_control.data.AppData

/**
 *创建者：ZyOng
 *描述：
 *创建时间：2025/7/4 下午3:57
 */
abstract class BaseManager {
    var appInfoList: ArrayList<AppData>? = null
    fun setAppList(appInfoList: ArrayList<AppData>?){
        this.appInfoList = appInfoList
    }
    abstract fun start() :AgentResult
    fun isChinaMobileInstalled(appPackage:String,context: Activity): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(appPackage))
        return intent.resolveActivity(context.packageManager) != null
    }
}