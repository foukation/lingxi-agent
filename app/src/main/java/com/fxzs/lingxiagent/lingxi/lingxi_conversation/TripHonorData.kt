package com.fxzs.lingxiagent.lingxi.lingxi_conversation

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

enum class ErrorCode (val alias: String) {
    SUC("0"),
}

enum class ContentType (val alias: String) {
    RICH_TEXT("rich_text"),
    CARD("card"),
    THINK("think"),
}
enum class ContentThinkType (val alias: String) {
    RICH_TEXT("think"),
    CARD("card"),
}

enum class CardType (val alias: Number) {
    ANDROID(1),
    WEB(2),
}

enum class MessageRole (val alias: String) {
    USER("user"),
    ASSISTANT("assistant"),
}

enum class MessageType (val alias: String) {
    TEXT("text")
}

data class TripHonorRes(
    val errorCode: String,
    val errorMessage: String,
    val choices: ChoicesData,
    val sessionId: String,
)

data class ChoicesData(
    val message: MessageData,
    val finishReason: String,
)

data class MessageData(
    val contentType: String,
    val hybridContent: HybridContentData,
)

data class HybridContentData(
    val commands: CommandsData,
)

data class HeadData(
    val namespace: String,
)

data class CommandsData(
    val head: HeadData,
    val body: BodyData,
)

data class BodyData(
    val text: String?,
    val cardType: Number?,
    val jsCards: ArrayList<CardData>?,
    val buttons: ArrayList<ButtonsData>?,
    val htmls: ArrayList<HtmlInfo>?
)

data class CardData(
    val templateId: String?,
    val serviceId: String?,
    val content: String,
    var type: String,
)

data class ButtonsData(
    val web: WebData,
)

data class WebData(
    val url: String,
)

@Parcelize
data class CardContentList(
    val list: @RawValue ArrayList<CardContent>,
    var moreLink: ItemLink,
): Parcelable

data class CardContent(
    val img: String,
    var title: String,
    var score: String,
    var tag: ArrayList<String>,
    var address: String,
    var subTag: ArrayList<String>,
    var itemLink: ItemLink,
    var buttonLink: ButtonLink
)

@Parcelize
data class ItemLink(
    val web: WebContent,
): Parcelable

@Parcelize
data class WebContent(
    val url: String,
): Parcelable

@Parcelize
data class ButtonLink(
    val nativeApp: NativeAppData,
    val text: String,
): Parcelable

@Parcelize
data class HtmlInfo(
    val url: String,
    val mode: Int,
    val height: Int,
    val width: Int
): Parcelable

@Parcelize
data class NativeAppData(
    var url: String,
    var appName: String,
    var pkgName: String,
): Parcelable
