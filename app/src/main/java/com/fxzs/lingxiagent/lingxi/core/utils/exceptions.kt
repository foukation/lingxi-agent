package cn.vove7.auto.core.utils

import cn.vove7.auto.core.viewfinder.ViewFinder
import com.fxzs.lingxiagent.lingxi.core.utils.AutoGestureDescription

/**
 * # exceptions
 * 异常类合集
 */

/**
 * 视图搜索失败异常
 */
class ViewNodeNotFoundException : Exception {
    constructor(finder: ViewFinder<*>)
        : super("ViewNodeNotFound: ${finder.finderInfo()}")

    constructor(msg: String) : super(msg)
}

class GestureCanceledException(
    val gestureDescription: AutoGestureDescription
) : RuntimeException()

class AutoServiceUnavailableException : RuntimeException() {

    override fun toString(): String {
        return "AutoServiceUnavailableException"
    }
}
