package com.fxzs.lingxiagent.lingxi.lingxi_conversation;

import android.annotation.SuppressLint
import com.cmdc.ai.assist.AIAssistantManager
import com.cmdc.ai.assist.api.AIFoundationKit
import com.cmdc.ai.assist.api.AISessionManager
import com.cmdc.ai.assist.api.AISessionManager.buildMessagesInsideRcChat
import com.cmdc.ai.assist.constraint.DialogueResult
import com.cmdc.ai.assist.constraint.InsideRcChatRequest
import com.fxzs.lingxiagent.conversation.AIConversationManager
import java.util.UUID

class ChatLingXiAdapter(
    private val aiConversationManager: AIConversationManager,
    private val requestId: String
) {
    private val aiFoundationKit = AIFoundationKit()
    private var messageIndex = 0

    @SuppressLint("BinaryOperationInTimber")
    fun insideRcChat(content: String, callback: (DialogueResult?) -> Any?) {
        try {

            val messages = AISessionManager.getChatDataList().buildMessagesInsideRcChat()
            messages.add(
                InsideRcChatRequest.Message(
                    role = "user",
                    content
                )
            )

            aiFoundationKit.insideRcChat(
                InsideRcChatRequest(
                    qid = UUID.randomUUID().toString(),
                    third_user_id = UUID.randomUUID().toString(),
                    cuid = AIAssistantManager.getInstance().aiAssistConfig.deviceId,
                    messages = messages,
                    stream = true,
                    dialog_request_id = UUID.randomUUID().toString()
                ),
                { response ->
                    callback(response)
                    if (messageIndex == 0 && response.is_end == 0) {
                        aiConversationManager.startStreaming(requestId)
                        messageIndex++
                    }
                    if (response.is_end == 1) {
                        aiConversationManager.endConversation(requestId)
                        messageIndex++
                    }
                }, { error ->
                    println(error)
                    callback(null)
                    aiConversationManager.endConversation(requestId)
                }
            )
        } catch (e: Exception) {
            callback(null)
            println(e.message)
            aiConversationManager.endConversation(requestId)
        }
    }
}