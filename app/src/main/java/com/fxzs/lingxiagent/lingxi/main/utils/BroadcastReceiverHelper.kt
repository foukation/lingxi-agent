package com.fxzs.lingxiagent.lingxi.main.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.fxzs.lingxiagent.IYAApplication
import com.fxzs.lingxiagent.model.chat.dto.ChatMessage
import com.fxzs.lingxiagent.viewmodel.chat.VMChat
import timber.log.Timber
import java.util.Objects

class BroadcastReceiverHelper(private val vmChat: VMChat) {

    private lateinit var localReceiver: BroadcastReceiver
    private val TAG: String = BroadcastReceiverHelper::class.java.simpleName
    var aiMessage: ChatMessage? = null

    init {
        setupLocalBroadcastReceiver()
    }

    private fun setupLocalBroadcastReceiver() {

        Timber.tag(TAG).i("setupLocalBroadcastReceiver")

        localReceiver = object : BroadcastReceiver() {
            var reply: String? = ""

            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                when (Objects.requireNonNull<String?>(action)) {
                    BroadcastConstants.ACTION_SPEECH_START -> {
                        Timber.tag(TAG).i("接收到：%s", BroadcastConstants.ACTION_SPEECH_START)
                    }

                    BroadcastConstants.ACTION_TRANSLATION_START -> {
                        Timber.tag(TAG).i("接收到：%s", BroadcastConstants.ACTION_TRANSLATION_START)
                    }

                    BroadcastConstants.ACTION_SPEECH_MID_RESULT -> {
                        Timber.tag(TAG).i("接收到：%s", BroadcastConstants.ACTION_SPEECH_MID_RESULT)
                    }

                    BroadcastConstants.ACTION_SPEECH_FINAL_RESULT -> {
                        Timber.tag(TAG)
                            .i("接收到：%s", BroadcastConstants.ACTION_SPEECH_FINAL_RESULT)
                    }

                    BroadcastConstants.ACTION_SEND -> {
                        Timber.tag(TAG).i("接收到：%s", BroadcastConstants.ACTION_SEND)
                    }

                    BroadcastConstants.ACTION_IMAGE_URI_RESPONSE -> {
                        Timber.tag(TAG).i("接收到：%s", BroadcastConstants.ACTION_IMAGE_URI_RESPONSE)
                    }

                    BroadcastConstants.ACTION_TEX_RESPONSE -> {
                        Timber.tag(TAG).i("接收到：%s", BroadcastConstants.ACTION_TEX_RESPONSE)
                    }

                    BroadcastConstants.ACTION_TEXT_REPLY -> {
                        Timber.tag(TAG).i("接收到：%s", BroadcastConstants.ACTION_TEXT_REPLY)
                    }

                    BroadcastConstants.ACTION_IMG_REPLY -> {
                        Timber.tag(TAG).i("接收到：%s", BroadcastConstants.ACTION_IMG_REPLY)
                    }

                    BroadcastConstants.ACTION_IMGS_REPLY -> {
                        Timber.tag(TAG).i("接收到：%s", BroadcastConstants.ACTION_IMGS_REPLY)
                    }

                    BroadcastConstants.ACTION_MEDIA_REPLY -> {
                        Timber.tag(TAG).i("接收到：%s", BroadcastConstants.ACTION_MEDIA_REPLY)
                    }

                    BroadcastConstants.ACTION_TRIP_CARD_REPLY -> {
                        Timber.tag(TAG).i("接收到：%s", BroadcastConstants.ACTION_TRIP_CARD_REPLY)
                    }

                    BroadcastConstants.ACTION_CHAT_TYPE_WRITE -> {
                        Timber.tag(TAG).i("接收到：%s", BroadcastConstants.ACTION_CHAT_TYPE_WRITE)
                        val content = intent.getStringExtra("content")
                        val isFinish = intent.getBooleanExtra("isFinish", false)
                        Timber.tag(TAG).i("接收到：%s", content)
                        Timber.tag(TAG).i("接收到：%s", isFinish)
                        if (aiMessage == null) {
                            aiMessage = vmChat.addAIMsg()
                            vmChat.initSendState(aiMessage)
                        }
                        vmChat.receiveResponse(aiMessage, content)
                    }

                    BroadcastConstants.ACTION_CHAT_TYPE_COT -> {
                        Timber.tag(TAG).i("接收到：%s", BroadcastConstants.ACTION_CHAT_TYPE_COT)
                    }

                    BroadcastConstants.ACTION_TRAVEL_HTML_REPLY -> {
                        Timber.tag(TAG).i("接收到：%s", BroadcastConstants.ACTION_TRAVEL_HTML_REPLY)
                    }

                    BroadcastConstants.ACTION_COMPLETE -> {
                        Timber.tag(TAG).i("接收到：%s", BroadcastConstants.ACTION_COMPLETE)
                        vmChat.endResponse(aiMessage)
                        aiMessage = null
                    }

                    BroadcastConstants.ACTION_COMPLETE_SENDTRIPCHATMESSAGE -> {
                        Timber.tag(TAG)
                            .i("接收到：%s", BroadcastConstants.ACTION_COMPLETE_SENDTRIPCHATMESSAGE)
                    }

                    BroadcastConstants.ACTION_COMPLETE_SENDTRIPCARD -> {
                        Timber.tag(TAG)
                            .i("接收到：%s", BroadcastConstants.ACTION_COMPLETE_SENDTRIPCARD)
                    }

                    BroadcastConstants.ACTION_ACCESSIBILITYCARD -> {
                        Timber.tag(TAG).i("接收到：%s", BroadcastConstants.ACTION_ACCESSIBILITYCARD)
                    }

                    BroadcastConstants.ACTION_SCROLL_BOTTOM -> {
                        Timber.tag(TAG).i("接收到：%s", BroadcastConstants.ACTION_SCROLL_BOTTOM)
                    }

                    BroadcastConstants.ACTION_CLOSE_ASR -> {
                        Timber.tag(TAG).i("接收到：%s", BroadcastConstants.ACTION_CLOSE_ASR)
                    }

                    BroadcastConstants.ACTION_SEND_TEXT -> {
                        Timber.tag(TAG).i("接收到：%s", BroadcastConstants.ACTION_SEND_TEXT)
                    }

                    BroadcastConstants.ACTION_STOP_SENDTRIPCARD -> {
                        Timber.tag(TAG).i("接收到：%s", BroadcastConstants.ACTION_STOP_SENDTRIPCARD)
                    }
                }
            }
        }

        val intentFilter: IntentFilter = getIntentFilter()
        LocalBroadcastManager.getInstance(IYAApplication.getInstance().applicationContext)
            .registerReceiver(localReceiver, intentFilter)
    }

    private fun getIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BroadcastConstants.ACTION_SPEECH_START)
        intentFilter.addAction(BroadcastConstants.ACTION_TRANSLATION_START)
        intentFilter.addAction(BroadcastConstants.ACTION_SPEECH_MID_RESULT)
        intentFilter.addAction(BroadcastConstants.ACTION_SPEECH_FINAL_RESULT)
        intentFilter.addAction(BroadcastConstants.ACTION_SEND)
        intentFilter.addAction(BroadcastConstants.ACTION_SHOW_KEYBOARD)

        intentFilter.addAction(BroadcastConstants.ACTION_IMAGE_URI_RESPONSE)
        intentFilter.addAction(BroadcastConstants.ACTION_TEX_RESPONSE)

        intentFilter.addAction(BroadcastConstants.ACTION_TEXT_REPLY)
        intentFilter.addAction(BroadcastConstants.ACTION_IMG_REPLY)
        intentFilter.addAction(BroadcastConstants.ACTION_IMGS_REPLY)
        intentFilter.addAction(BroadcastConstants.ACTION_MEDIA_REPLY)
        intentFilter.addAction(BroadcastConstants.ACTION_TRIP_CARD_REPLY)
        intentFilter.addAction(BroadcastConstants.ACTION_TRAVEL_HTML_REPLY)
        intentFilter.addAction(BroadcastConstants.ACTION_CHAT_TYPE_WRITE)
        intentFilter.addAction(BroadcastConstants.ACTION_CHAT_TYPE_COT)

        intentFilter.addAction(BroadcastConstants.ACTION_COMPLETE)
        intentFilter.addAction(BroadcastConstants.ACTION_COMPLETE_SENDTRIPCHATMESSAGE)
        intentFilter.addAction(BroadcastConstants.ACTION_COMPLETE_SENDTRIPCARD)
        intentFilter.addAction(BroadcastConstants.ACTION_ACCESSIBILITYCARD)
        intentFilter.addAction(BroadcastConstants.ACTION_SCROLL_BOTTOM)
        intentFilter.addAction(BroadcastConstants.ACTION_AUTO_HEIGHT)
        intentFilter.addAction(BroadcastConstants.ACTION_CLOSE_ASR)

        intentFilter.addAction(BroadcastConstants.ACTION_SEND_TEXT)
        intentFilter.addAction(BroadcastConstants.ACTION_STOP_SENDTRIPCARD)
        return intentFilter
    }

    fun unregisterLocalReceiver() {
        Timber.tag(TAG).i("unregisterLocalReceiver")
        LocalBroadcastManager.getInstance(IYAApplication.getInstance().applicationContext)
            .unregisterReceiver(localReceiver)
    }

}