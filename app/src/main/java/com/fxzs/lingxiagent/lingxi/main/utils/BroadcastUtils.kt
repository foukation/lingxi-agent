package com.fxzs.lingxiagent.lingxi.main.utils

import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.service_api.data.CardContentList
import com.example.service_api.data.HtmlInfo
import com.fxzs.lingxiagent.IYAApplication
import com.fxzs.lingxiagent.lingxi.config.PermissionType

object BroadcastUtils {

    fun completeBroadcast() {
        val broadcastIntent = Intent(BroadcastConstants.ACTION_COMPLETE)
        LocalBroadcastManager.getInstance(IYAApplication.getInstance()).sendBroadcast(broadcastIntent)
    }

    fun replyMessageBroadcast(message: String, shouldMerge: Boolean = true, isShowBtn: Boolean = false) {
        val broadcastIntent = Intent(BroadcastConstants.ACTION_TEXT_REPLY)
        broadcastIntent.putExtra("message", message)
        broadcastIntent.putExtra("shouldMerge", shouldMerge)
        broadcastIntent.putExtra("isShowBtnParam", isShowBtn)
        LocalBroadcastManager.getInstance(IYAApplication.getInstance()).sendBroadcast(broadcastIntent)
    }

    fun  replyTypeWriteContentBroadcast(message: String) {
        replyMessageBroadcast(message, false)
    }

    fun replyMessageBroadcast(content: String, isFinish: Boolean) {
        val broadcastIntent = Intent(BroadcastConstants.ACTION_CHAT_TYPE_WRITE)
        broadcastIntent.putExtra("content", content)
        broadcastIntent.putExtra("isFinish", isFinish)
        LocalBroadcastManager.getInstance(IYAApplication.getInstance()).sendBroadcast(broadcastIntent)
    }

    fun closeAsrBroadcast() {
        val broadcastIntent = Intent(BroadcastConstants.ACTION_CLOSE_ASR)
        LocalBroadcastManager.getInstance(IYAApplication.getInstance()).sendBroadcast(broadcastIntent)
    }

    fun replyImgBroadcast(url: String) {
        val broadcastIntent = Intent(BroadcastConstants.ACTION_IMG_REPLY)
        broadcastIntent.putExtra("imgPath", url)
        LocalBroadcastManager.getInstance(IYAApplication.getInstance()).sendBroadcast(broadcastIntent)
    }

    fun replyImagesBroadcast(imagesPath: ArrayList<String>) {
        val broadcastIntent = Intent(BroadcastConstants.ACTION_IMGS_REPLY)
        broadcastIntent.putExtra("imagesPath", imagesPath)
        LocalBroadcastManager.getInstance(IYAApplication.getInstance()).sendBroadcast(broadcastIntent)
    }

    fun replyMediaBroadcast(mediaTitle: String, mediaUrl: String) {
        val broadcastIntent = Intent(BroadcastConstants.ACTION_MEDIA_REPLY)
        broadcastIntent.putExtra("mediaTitle", mediaTitle)
        broadcastIntent.putExtra("mediaUrl", mediaUrl)
        LocalBroadcastManager.getInstance(IYAApplication.getInstance()).sendBroadcast(broadcastIntent)
    }

    fun replyTripCardBroadcast(content: String) {
        val broadcastIntent = Intent(BroadcastConstants.ACTION_TRIP_CARD_REPLY)
        broadcastIntent.putExtra("content", content)
        LocalBroadcastManager.getInstance(IYAApplication.getInstance()).sendBroadcast(broadcastIntent)
    }

    fun replyTravelHtmlBroadcast(htmlInfo: HtmlInfo) {
        val broadcastIntent = Intent(BroadcastConstants.ACTION_TRAVEL_HTML_REPLY)
        broadcastIntent.putExtra("htmlInfo", htmlInfo)
        LocalBroadcastManager.getInstance(IYAApplication.getInstance()).sendBroadcast(broadcastIntent)
    }

    fun replyCotContentBroadcast(content: String) {
        val broadcastIntent = Intent(BroadcastConstants.ACTION_CHAT_TYPE_COT)
        broadcastIntent.putExtra("content", content)
        LocalBroadcastManager.getInstance(IYAApplication.getInstance()).sendBroadcast(broadcastIntent)
    }

    fun replyPermissionCardBroadcast(type: PermissionType) {
        val broadcastIntent = Intent(BroadcastConstants.ACTION_ACCESSIBILITYCARD)
        broadcastIntent.putExtra("PermissionType", type)
        LocalBroadcastManager.getInstance(IYAApplication.getInstance()).sendBroadcast(broadcastIntent)
    }

    fun viewScrollBottomBroadcast() {
        val broadcastIntent = Intent(BroadcastConstants.ACTION_SCROLL_BOTTOM)
        LocalBroadcastManager.getInstance(IYAApplication.getInstance()).sendBroadcast(broadcastIntent)
    }

    fun speechFinishBroadcast() {
        val broadcastIntent = Intent(BroadcastConstants.ACTION_SPEECH_FINAL_RESULT)
        LocalBroadcastManager.getInstance(IYAApplication.getInstance()).sendBroadcast(broadcastIntent)
    }

    fun completeHonorCardBroadcast() {
        val broadcastIntent = Intent(BroadcastConstants.ACTION_COMPLETE_SENDTRIPCARD)
        LocalBroadcastManager.getInstance(IYAApplication.getInstance()).sendBroadcast(broadcastIntent)
    }

    fun stopHonorCardBroadcast() {
        val broadcastIntent = Intent(BroadcastConstants.ACTION_STOP_SENDTRIPCARD)
        LocalBroadcastManager.getInstance(IYAApplication.getInstance()).sendBroadcast(broadcastIntent)
    }

}