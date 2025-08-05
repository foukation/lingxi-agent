package com.example.service_api

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.service_api.IntentionApi.gson
import com.example.service_api.config.HonorConf
import com.example.service_api.data.MessageRole
import com.example.service_api.data.TripHonorRes
import com.example.service_api.map.GMapHelper
import com.fxzs.lingxiagent.BuildConfig
import com.fxzs.lingxiagent.lingxi.main.utils.DeviceUUIDGenerator
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class HttpUrlConnectionHonor(private var context: Context) {
    private val TAG = "HttpUrlConnectionHonor"
    private lateinit var connection: HttpURLConnection
    private var messages: JSONArray = JSONArray()
    private lateinit var sessionId: String
    private var apiUrl: String = ""
    private var interruptMessage = false

    init {
        updateSession()
    }

    fun updateRequestInfo(apiUrl: String) {
        if (this.apiUrl != apiUrl) {
            this.apiUrl = apiUrl
            updateSession()
        }
    }

    fun getHonorAgentType(): String {
        return this.apiUrl
    }

    fun updateSession() {
        this.sessionId = (System.currentTimeMillis() + (System.nanoTime() % 1_000_000)).toString()
        this.messages = JSONArray()
        this.interruptMessage = false
    }

    fun updateMessages(role: String, content: String, type: String) {
        val message = JSONObject()
        message.put("role", role)
        message.put("content", content)
        message.put("type", type)

        messages.put(message)
    }

    // 流式数据处理器
    interface StreamHandler {
        fun onDataChunk(resp: TripHonorRes)
        fun onStreamComplete()
        fun onError(errMsg: String)
        fun onStreamStop()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendStreamRequest(inputString: String, handler: StreamHandler) {
        this.interruptMessage = false
        val timestamp = System.currentTimeMillis() + (System.nanoTime() % 1_000_000)
        val signature = generateSign(timestamp.toString())
        val url = URL(apiUrl)
        connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("accessKey", BuildConfig.HONOR_ACCESS_KEY)
        connection.setRequestProperty("ts", timestamp.toString())
        connection.setRequestProperty("sign", signature)
        connection.doOutput = true

        val requestBody = createRequestBody(inputString, sessionId, timestamp.toString())
        val payloadBytes = requestBody.toString().toByteArray(StandardCharsets.UTF_8)

        try {
            connection.outputStream.use { it.write(payloadBytes) }
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                handleErrorResponse(handler)
                return
            }
            handleStreamResponse(handler)
        } catch (e: IOException) {
            if (interruptMessage){
                handler.onError("网络请求失败: ${e.message}")
            }else{
                handler.onStreamStop()
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun handleStreamResponse(handler: StreamHandler) {
        BufferedReader(
            InputStreamReader(connection.inputStream, StandardCharsets.UTF_8)
        ).use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                Timber.tag(HonorConf.TAG).d("流式数据: $line")
                if (interruptMessage){
                    Timber.tag(HonorConf.TAG).d("中断流式数据")
                    handler.onStreamStop()
                    break
                }
                when {
                    line?.contains("[DONE]") == true -> {
                        Timber.tag(HonorConf.TAG).d("[Stream completed]")
                        handler.onStreamComplete()
                    }
                    line?.startsWith("data:") == true -> {
                        val dataStr = line?.substring(5)?.trim()
                        handleDataChunk(dataStr, handler)
                    }
                }
            }
        }
    }

    @SuppressLint("TimberArgCount")
    private fun handleDataChunk(dataStr: String?, handler: StreamHandler) {
        if (dataStr != null) {
            if (dataStr.isBlank()) return
        }
        try {
            val resp = gson.fromJson(dataStr, TripHonorRes::class.java)
            handler.onDataChunk(resp)
        } catch (e: Exception) {
            Timber.tag(HonorConf.TAG).e(e, "%s%s", "数据解析失败: ", dataStr)
            handler.onError("数据解析失败: ${e.message}")
        }
    }

    private fun handleErrorResponse(handler: StreamHandler) {
        val errorStream = connection.errorStream ?: return
        BufferedReader(InputStreamReader(errorStream)).use { reader ->
            val errorMsg = reader.readText()
            Timber.tag(HonorConf.TAG).e("错误响应: $errorMsg")
            handler.onError("服务端错误: $errorMsg")
        }
    }

    // 保持原有请求体创建逻辑
    private fun createRequestBody(inputString: String, sessionId: String, ts: String): JSONObject {
        val data = JSONObject()
        data.put("requestId", ts)
        data.put("model", "qwen2.5-vl-32b-instruct")
        data.put("stream", true)
        data.put("temperature", 0.7)
        data.put("top_p", 0.9)
        data.put("max_tokens", 2048)
        val endpoint = JSONObject()
        val location = JSONObject()
        val device = JSONObject()
        val base = JSONObject()
        val deviceUuid = DeviceUUIDGenerator.getDeviceUUID(context)

        endpoint.put("location", location)
        location.put("longitude", GMapHelper.longitude.toString());
        location.put("latitude", GMapHelper.latitude.toString());
        location.put("locationSystem", "GCJ02");

        endpoint.put("device", device)
        device.put("base", base);
        base.put("deviceUuid", deviceUuid);
        data.put("endpoint", endpoint)

        val session = JSONObject()
        session.put("sessionId", sessionId)
        session.put("attributes", "{\"key\":\"value\"}")
        data.put("session", session)

        updateMessages(MessageRole.USER.alias, inputString, "text")
        data.put("messages", messages)
        Timber.tag(TAG).i("请求体: $data")
        return data
    }

    // 保持原有签名生成逻辑
    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateSign(timestamp: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        val secretKey2 = SecretKeySpec(BuildConfig.HONOR_SECRET_KEY.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
        mac.init(secretKey2)
        val byteHMAC = mac.doFinal(timestamp.toByteArray(StandardCharsets.UTF_8))
        return Base64.getEncoder().encodeToString(byteHMAC)
    }

    fun interruptMessage(){
        if (::connection.isInitialized){
            connection.disconnect()
        }
        this.interruptMessage = true

    }
}