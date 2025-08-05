package com.fxzs.lingxiagent.lingxi.translate

import android.content.Context
import android.content.Intent
import com.cmdc.ai.assist.AIAssistantManager
import com.cmdc.ai.assist.api.ASRTranslation
import com.cmdc.ai.assist.constraint.TranslationData
import com.cmdc.ai.assist.constraint.TranslationTypeCode
import timber.log.Timber
import java.nio.ByteBuffer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.cmdc.ai.assist.constraint.Language

class TranslationManager(private val context: Context,callBack:(status:Boolean)->Unit) {

    private val TAG: String = TranslationManager::class.java.simpleName

    private var asrTranslation: ASRTranslation? = null

    init {

        asrTranslation = AIAssistantManager.getInstance().asrTranslationHelp() as ASRTranslation

        asrTranslation?.setListener(object : ASRTranslation.ASRTranslationListener {
            override fun onMessageReceived(message: TranslationData?) {
                Timber.tag(TAG).d("type： ${message?.type}")
                Timber.tag(TAG).d("asrResult： ${message?.asrResult}")
                Timber.tag(TAG).d("translationResult： ${message?.translationResult}")

                if (message?.type.equals("MID")) {
                    message?.asrResult?.let {
                        sendAsrMidResultMessage(
                            context.applicationContext,
                            it
                        )
                    }
                    return
                }
                if (message?.type.equals("FIN")) {
                    message?.asrResult?.let {
                        sendAsrMidResultMessage(
                            context.applicationContext,
                            it
                        )
                    }
                    message?.asrResult?.let {
                        sendAsrFinalResultMessage(
                            context.applicationContext,
                        )
                    }
                    message?.translationResult?.let {
                        sendTranslationResultMessage(
                            context.applicationContext,
                            it
                        )
                    }
                    message?.translationResult?.let {
                        completeBroadcast(
                            context.applicationContext,
                        )
                    }
                    callBack.invoke(true)
                    asrTranslation?.release()
                    return
                }

            }

            override fun onMessageReceived(bytes: ByteBuffer?) {
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Timber.tag(TAG).d("onClose： $reason")
            }

            override fun onError(ex: Exception?) {
                Timber.tag(TAG).d("onError： $ex")
            }
        })

    }

    private fun translationZH_TO_EN() {
        asrTranslation?.startRecognition(TranslationTypeCode.ZH_TO_EN)
    }

    private fun translationEN_TO_ZH() {
        asrTranslation?.startRecognition(TranslationTypeCode.EN_TO_ZH)
    }

    fun translation(fromLang: String, toLang: String) {
        asrTranslation?.startRecognition(convertLanguageCode(fromLang), convertLanguageCode(toLang))
    }

    private fun sendAsrMidResultMessage(context: Context, message: String) {
        val broadcastIntent = Intent("com.fxzs.lingxiagent.KEY_MID_RESULT")
        broadcastIntent.putExtra(
            "mid_result",
            message
        )
        LocalBroadcastManager.getInstance(context.applicationContext)
            .sendBroadcast(broadcastIntent)
    }

    private fun sendAsrFinalResultMessage(context: Context) {
        val broadcastIntent = Intent("com.fxzs.lingxiagent.KEY_FINAL_RESULT")
        LocalBroadcastManager.getInstance(context.applicationContext)
            .sendBroadcast(broadcastIntent)
    }


    private fun sendTranslationResultMessage(
        context: Context,
        message: String,
        shouldMerge: Boolean = true,
        isShowBtn: Boolean = false
    ) {
        val broadcastIntent = Intent("com.fxzs.lingxiagent.textReply")
        broadcastIntent.putExtra("message", message)
        broadcastIntent.putExtra("shouldMerge", shouldMerge)
        broadcastIntent.putExtra("isShowBtnParam", isShowBtn)
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent)
    }

    private fun completeBroadcast(context: Context) {
        val broadcastIntent = Intent("com.fxzs.lingxiagent.complete")
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent)
    }

    private fun convertLanguageCode(language: String): Language {
        if (language == "中文") return Language.CHINESE
        if (language == "英语") return Language.ENGLISH
        if (language == "日语") return Language.JAPANESE
        if (language == "韩语") return Language.KOREAN
        if (language == "西班牙语") return Language.SPANISH
        if (language == "法语") return Language.FRENCH
        if (language == "德语") return Language.GERMAN
        if (language == "俄语") return Language.RUSSIAN
        if (language == "意大利语") return Language.ITALIAN
        return Language.CHINESE
    }

}