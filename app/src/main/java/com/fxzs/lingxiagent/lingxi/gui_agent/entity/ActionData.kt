package com.skythinker.gui_agent.entity


enum class ActionType (val alias: String) {
    CLICK("click"),
    INPUT("input"),
    LONG_PRESS("long_press"),
    PRESS("press"),
    OPEN_APP("open_app"),
    SCROLL("scroll"),
    DRAG("drag"),
    FINISHED("finished");
}

enum class OperatorType(val keyEvent: String) {
    HOME("HOME"),
    BACK("BACK"),
    MENU("MENU"),
    ENTER("ENTER"),
    APPSELECT("APPSELECT"),
    CLEAR("CLEAR"),
    POWER("power"),
    VOLUME_UP("volume_up"),
    VOLUME_DOWN("volume_down"),
    VOLUME_MUTE("volume_mute");
}

enum class ScrollDirection(val alias: String) {
    UP("up"),
    DOWN("down"),
    LEFT("left"),
    RIGHT("right");
}