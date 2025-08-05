package com.skythinker.gui_agent.entity


enum class TaskStatus (val alias: Int) {

    SUCCESS(10000),//成功
    SCREEN_SHOT_ERROR(10001), //截图失败
    ACCESSIBILITY_NO_OPEN(10002),//无障碍服务未开启
    OPEN_APP(10003),//打开app
    NETWORK_ERROR(10004),//网络错误
    MULTIPLE_CONVERSATION(10005),//多轮会话
    MULTIPLE_OPERATIONS(10006),//多次操作
    OPEN_MENU(10007),//打开按钮
    CLOSE_MENU(10008),//关闭按钮
    EXCEPTION(10009),//异常
    TASK_INTERRUPTION(10010),//任务中断
    TASK_EXECUTE_FAIL(10011),//任务执行失败



}

enum class ExecuteStatus (val alias: Int) {
    INPUT(10100),//输入
    OPEN_APP_FAIL(10101)//打开应用失败



}
