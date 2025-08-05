package com.fxzs.lingxiagent.helper

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.fxzs.lingxiagent.IYAApplication
import com.fxzs.lingxiagent.layout.ModelType
import com.fxzs.lingxiagent.lingxi.config.MenuHandlerCallBack
import com.fxzs.lingxiagent.lingxi.main.callback.TaskHandlerCallback
import com.fxzs.lingxiagent.lingxi.main.service.FloatViewModelService
import com.fxzs.lingxiagent.service.FloatWindowService
import com.fxzs.lingxiagent.service.MenuActionFloatService
import com.itextpdf.kernel.pdf.PdfName.App

object FloatHelper {
    private class Connection(val description: String = "", val modelType: ModelType = ModelType.DEFAULT, val close: () -> Unit = {}, val stop: () -> Unit = {}, val stopText: String = "停止", val closeText: String = "关闭", var tag:String="")  : ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val floatViewModelService = (service as FloatViewModelService.ViewModelBinder).getService()
            floatViewModelService.setTaskHandlerCallback(object : TaskHandlerCallback {
                override fun onClose() {
                    close()
                }
                override fun onStop() {
                    stop()
                }
            }, modelType, description, stopText, closeText, tag)
        }
        override fun onServiceDisconnected(name: ComponentName?) {}
    }

    private var connection: Connection? = null
    private var menuConnection: MenuConnection? = null

    fun modelToast(description: String){
        val intent = Intent(IYAApplication.getInstance(), FloatViewModelService::class.java)
        connection = Connection(description = description, modelType = ModelType.TASK_TOAST)
        IYAApplication.getInstance().applicationContext.bindService(intent, connection!!, Context.BIND_AUTO_CREATE)
    }

    fun actionToast(description: String, close:()->Unit, stop:()->Unit, stopText: String = "停止", closeText: String = "关闭",) {
        val intent = Intent(IYAApplication.getInstance(), FloatViewModelService::class.java)
        connection = Connection( description, ModelType.DEFAULT, close, stop, stopText, closeText)
        IYAApplication.getInstance().applicationContext.bindService(intent, connection!!, Context.BIND_AUTO_CREATE)
    }

    fun closeModel() {
        if (connection != null) {
            IYAApplication.getInstance().applicationContext.unbindService(connection!!)
            connection = null
        }
    }

    private class MenuConnection(val close: () -> Unit = {})  : ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val service = (service as MenuActionFloatService.ViewModelBinder).getService()
            service.setTaskHandlerCallback(object : MenuHandlerCallBack {
                override fun onClose() {
                    close()
                }
            })
        }
        override fun onServiceDisconnected(name: ComponentName?) {}
    }

    fun openFloatMenu(close:()->Unit) {
        val intent = Intent(IYAApplication.getInstance(), MenuActionFloatService::class.java)
        menuConnection = MenuConnection(close)
        IYAApplication.getInstance().stopService(Intent(IYAApplication.getInstance(), FloatWindowService::class.java))
        IYAApplication.getInstance().applicationContext.bindService(intent, menuConnection!!, Context.BIND_AUTO_CREATE)
    }

    fun closeFloatMenu() {
        if (menuConnection != null) {
            IYAApplication.getInstance().startService(Intent(IYAApplication.getInstance(), FloatWindowService::class.java))
            IYAApplication.getInstance().applicationContext.unbindService(menuConnection!!)
            menuConnection = null
        }
    }
}