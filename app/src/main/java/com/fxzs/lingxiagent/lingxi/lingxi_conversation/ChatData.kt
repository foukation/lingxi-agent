package com.fxzs.lingxiagent.lingxi.lingxi_conversation

enum class NameType (val alias: String) {
    NLU("Nlu"),
    PROCESS("RenderProcessing"),
    IMG_CARD("RenderMultiImageCard"),
    RENDER_FLOW("RenderStreamCard"),
    SPEAK("Speak"),
    PLAY("Play"),
    RENDER_HINT("RenderHint"),
    RENDER_VOICE_TEXT("RenderVoiceInputText"),
}

enum class IntentDomain (val alias: String) {
    CHAT("Chat"),
    AIGC("AIGC"),
    MEDIA("Media"),
    NAVIGATION("Navigation"),
    SYSTEM_CONTROL("SystemControl"),
    PHONE("Phone"),
    CAR_CONTROL("CarControl"),
    ALARM("Alarm"),
    SYS_PROFILE("SysProfile"),
    DRINK("Drink"),
    UNCLEAR("Unclear"),
    TELECOMSERVICE("TelecomService"),
    HEALTHCARE("Healthcare"),
    CUSTOMERSERVICE("CustomerService"),
    TRAVEL("Travel"),
    GATHERING("Gathering"),
    MEMBERSHIP("Membership"),
}

enum class ChatIntent (val alias: String) {
    TRANSLATION("Chat.Translation"),
    LLMQA("Chat.LLMQA"),
    BAIDU_BAIKE("Chat.BaiduBaike"),
    WEATHER("Chat.Weather"),
}

enum class SystemControlIntent (val alias: String) {
    Volume("SystemControl.Volume"),
    APP("SystemControl.APP"),
    Page("SystemControl.Page")
}

enum class ImgIntent (val alias: String) {
    AIGC_DRAW("AIGC.Draw"),
}

enum class MediaIntent (val alias: String) {
    MEDIA_MUSIC("Media.MusicPlay"),
    MEDIA_UNICAST("Media.UnicastPlay"),
    MEDIA_VIDEOPLY("Media.VideoPlay"),
}

enum class NavIntent (val alias: String) {
    NAV_NAV("Navigation.Navigator"),
    NAV_POI("Navigation.POISearch"),
    NAV_AIGuide("Navigation.AIGuide"),
}
enum class Travel (val alias: String) {
    Travel_BookTicket("Travel.BookTicket"),
    Travel_BookHotel("Travel.BookHotel"),
    Travel_PlanTravel("Travel.PlanTravel"),
    Travel_RecommendDestination("Travel.RecommendDestination"),
}
enum class GATHERING (val alias: String) {
    Gathering_SearchPOI("Gathering.SearchPOI"),
    Gathering_SelectPOI("Gathering.SelectPOI"),
    Gathering_Others("Gathering_Others"),
}

enum class LocalModule {
    CHAT,
    IMG,
    TRANSLATE,
    MEDIA,
    TRAVEL,
    GATHERING,
    TRIP,
    TRIP_HONOR,
    MEDICINE,
    ACTION,
    WEATHER,
    SYS_CONTROL,
    MUSIC,
    MEET,
}

enum class AdapterType {
    CHAT,
    COT,
    CARD,
}

enum class ServiceTemplate (val alias: String) {
    TRAIN("https://honor.tscfn.cn/h5/template/train.html?content="),
    PLANE("https://honor.tscfn.cn/h5/template/plane.html?content="),
    HOTEL("https://honor.tscfn.cn/h5/template/hotel.html?content="),
    HOME("https://honor.tscfn.cn/h5/template/home.html?content="),
    ORDER("https://honor.tscfn.cn/h5/template/order.html?content="),
}

enum class ServiceTemplateType (val alias: String) {
    TRAIN("CmdcTrainTicket"),
    PLANE("CmdcAirTicket"),
    HOTEL("CmdcHotel"),
    HOME("CmdcTourismPlan"),
    ORDER("CmdOrder"),
    FOOD("CmdcFoodCard"),
}