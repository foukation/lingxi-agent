package com.skythinker.gui_agent.util

import android.annotation.SuppressLint
import java.security.SecureRandom
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter



object SessionUtils {
    // 使用单例属性存储SecureRandom实例
    private val secureRandom: SecureRandom = SecureRandom()

    // 生成包含时间戳和随机数的Session ID
    @SuppressLint("NewApi")
    fun generateSessionId(): String {
        // 获取当前时间戳（精确到毫秒），使用java.time API
        val timestamp = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")
            .withZone(ZoneId.systemDefault()) // 可以根据需要指定时区
            .format(LocalDateTime.now())

        // 生成随机数部分（8位数字）
        val randomNum = secureRandom.nextInt(90000000) + 10000000

        return "$timestamp-$randomNum"
    }
}