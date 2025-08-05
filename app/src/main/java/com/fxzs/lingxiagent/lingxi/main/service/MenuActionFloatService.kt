package com.fxzs.lingxiagent.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.fxzs.lingxiagent.IYAApplication
import com.fxzs.lingxiagent.helper.FloatComponent
import com.fxzs.lingxiagent.helper.FloatType
import com.fxzs.lingxiagent.lingxi.config.MenuHandlerCallBack

class MenuActionFloatService : Service() {
    fun setTaskHandlerCallback(taskHandlerCallback: MenuHandlerCallBack) {
        FloatComponent.create(IYAApplication.getInstance(), FloatType.MENU_ACTION_FLOAT, null, close = { taskHandlerCallback.onClose() })
    }

    override fun onBind(intent: Intent?): IBinder? {
        return ViewModelBinder()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        FloatComponent.dismiss(FloatType.MENU_ACTION_FLOAT)
        return super.onUnbind(intent)
    }

    class ViewModelBinder: Binder() {
        fun getService():MenuActionFloatService{
            return MenuActionFloatService()
        }
    }
}