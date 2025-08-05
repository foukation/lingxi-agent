package com.fxzs.lingxiagent.helper

import com.example.service_api.IntentionApi
import com.example.service_api.data.AppData
import com.example.service_api.data.ClientApiAppListRes


object  AppListHelper {
    var appInfoList: ArrayList<AppData>? = null

    fun getAppList() {
        IntentionApi.handlerRequestClientAppList(
            onSuccess = fun (response: ClientApiAppListRes) {
                appInfoList = response.data.list
            },
            onError = fun (_: String) {
            }
        )
    }
}