package cn.vove7.auto.core.api

import android.accessibilityservice.AccessibilityService
import android.graphics.Path
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Pair
import android.view.ViewConfiguration
import cn.vove7.auto.core.AutoApi
import cn.vove7.auto.core.utils.GestureCanceledException
import cn.vove7.auto.core.utils.ScreenAdapter
import com.fxzs.lingxiagent.lingxi.core.utils.AutoGestureDescription
import com.fxzs.lingxiagent.lingxi.core.utils.GestureResultCallback
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * 手势api
 */
/**
 * 设置屏幕相对坐标
 * @param width Int
 * @param height Int
 */

fun setScreenSize(width: Int, height: Int) = ScreenAdapter.setScreenSize(width, height)

fun resetScreenSize() = ScreenAdapter.reset()

/**
 * 根据点坐标生成路径 执行手势
 * @param duration Long
 * @param points Array<Pair<Int, Int>>
 * @return Boolean
 */
suspend fun gesture(
    duration: Long,
    points: Array<Pair<Int, Int>>
): Boolean {
    val path = pointsToPath(points)
    return playGestures(listOf(AutoGestureDescription.StrokeDescription(path, 0, duration)))
}

/**
 * 根据点坐标列表生成路径 执行手势
 * @param duration Long
 * @param points List<Pair<Int, Int>>
 * @return Boolean
 */
suspend fun gesture(
    duration: Long,
    points: List<Pair<Int, Int>>
): Boolean {
    val path = pointsToPath(points.toTypedArray())
    return playGestures(listOf(AutoGestureDescription.StrokeDescription(path, 0, duration)))
}

/**
 * 根据Path执行手势
 * @param duration Long
 * @param path Path
 * @return Boolean
 */
suspend fun gesture(
    duration: Long, path: Path
) = gesture(duration, arrayOf(path))

/**
 * 多路径手势
 * @param duration Long
 * @param paths Array<Path>
 * @return Boolean
 */
suspend fun gesture(
    duration: Long, paths: Array<Path>
) = playGestures(
    paths.map { AutoGestureDescription.StrokeDescription(it, 0, duration) }
)

/**
 * api 异步手势
 * @param duration Long
 * @param points Array<Pair<Int, Int>>
 */
suspend fun gestureAsync(
    duration: Long, path: Path,
    callback: GestureResultCallback? = null
) = gestureAsync(duration, arrayOf(path), callback)

suspend fun gestureAsync(
    duration: Long, paths: Array<Path>,
    callback: GestureResultCallback? = null
) = doGesturesAsync(
    paths.map { AutoGestureDescription.StrokeDescription(it, 0, duration) },
    callback
)

suspend fun gestureAsync(
    duration: Long,
    points: Array<Pair<Int, Int>>,
    callback: GestureResultCallback? = null
) {
    val path = pointsToPath(points)
    doGesturesAsync(
        listOf(AutoGestureDescription.StrokeDescription(path, 0, duration)),
        callback
    )
}

/**
 * api 多路径手势
 * @param duration Long
 * @param ppss Array<Array<Pair<Int, Int>>>
 */
suspend fun gestures(
    duration: Long, ppss: Array<Array<Pair<Int, Int>>>,
): Boolean {
    val list = mutableListOf<AutoGestureDescription.StrokeDescription>()
    ppss.forEach {
        list.add(AutoGestureDescription.StrokeDescription(pointsToPath(it), 0, duration))
    }
    return playGestures(list)
}

/**
 * api 多路径手势 异步
 * @param duration Long
 * @param ppss Array<Array<Pair<Int, Int>>>
 */
suspend fun gesturesAsync(
    duration: Long, ppss: Array<Array<Pair<Int, Int>>>,
    callback: GestureResultCallback? = null
) {
    val list = mutableListOf<AutoGestureDescription.StrokeDescription>()
    ppss.forEach {
        list.add(AutoGestureDescription.StrokeDescription(pointsToPath(it), 0, duration))
    }
    doGesturesAsync(list, callback)
}

/**
 * 同步执行手势
 * @param strokeList Array<out StrokeDescription>
 * @return Boolean
 */
@Throws(GestureCanceledException::class)
suspend fun playGestures(
    strokeList: List<AutoGestureDescription.StrokeDescription>
): Boolean {
    val builder = AutoGestureDescription.Builder()
    strokeList.forEach(builder::addStroke)

    val handler = if (Looper.myLooper() == Looper.getMainLooper()) null
    else HandlerThread("ges").let {
        it.start()
        Handler(it.looper)
    }
    return suspendCoroutine { coroutine ->
        runBlocking {
            AutoApi.doGesturesAsync(builder.build(),
                object : GestureResultCallback() {
                    override fun onCompleted(gestureDescription: AutoGestureDescription) {
                        handler?.looper?.quitSafely()
                        coroutine.resume(true)
                    }

                    override fun onCancelled(gestureDescription: AutoGestureDescription) {
                        handler?.looper?.quitSafely()
                        coroutine.resumeWithException(GestureCanceledException(gestureDescription))
                    }
                }, handler
            )
        }
    }
}

/**
 * 点转路径
 * @param points Array<Pair<Int, Int>>
 * @return Path
 */
private fun pointsToPath(points: Array<Pair<Int, Int>>): Path {
    val path = Path()
    if (points.isEmpty()) return path
    path.moveTo(ScreenAdapter.scaleX(points[0].first), ScreenAdapter.scaleY(points[0].second))

    for (i in 1 until points.size) {
        path.lineTo(ScreenAdapter.scaleX(points[i].first), ScreenAdapter.scaleY(points[i].second))
    }
    return path
}

/**
 * 异步手势
 * @param strokeList List<out StrokeDescription>
 */
suspend fun doGesturesAsync(
    strokeList: List<AutoGestureDescription.StrokeDescription>,
    callback: GestureResultCallback?
) {
    val builder = AutoGestureDescription.Builder()
    strokeList.forEach(builder::addStroke)
    AutoApi.doGesturesAsync(builder.build(), callback, null)
}

suspend fun click(x: Int, y: Int): Boolean =
    pressWithTime(x, y, ViewConfiguration.getTapTimeout() + 50)


private fun Result<Boolean>.holdGestureResult() =
    onFailure(Timber::w).fold(onFailure = { false }, onSuccess = { it })


/**
 *
 * @param x Int
 * @param y Int
 * @param delay Int
 * @return Boolean
 */
suspend fun pressWithTime(x: Int, y: Int, delay: Int) =
    gesture(delay.toLong(), arrayOf(Pair(x, y)))

suspend fun pressOn(points: List<Pair<Int, Int>>) =
    gesture(300, points)

/**
 * 长按 相对坐标
 * @param x Int
 * @param y Int
 * @return Boolean
 */
suspend fun longClick(x: Int, y: Int) = pressWithTime(
    x, y, (ViewConfiguration.getLongPressTimeout() + 200)
)

/**
 * 两点间滑动
 * @param x1 Int
 * @param y1 Int
 * @param x2 Int
 * @param y2 Int
 * @param dur Int
 * @return Boolean
 */
suspend fun swipe(
    x1: Int, y1: Int,
    x2: Int, y2: Int,
    dur: Int
): Boolean = gesture(
    dur.toLong(), arrayOf(
    Pair(x1, y1),
    Pair(x2, y2)
))

suspend fun scrollUp(): Boolean {
    val mtop = (ScreenAdapter.relHeight * 0.1).toInt()
    val mBottom = (ScreenAdapter.relHeight * 0.85).toInt()
    val xCenter = (ScreenAdapter.relWidth * 0.5).toInt()
    return swipe(xCenter, mBottom, xCenter, mtop, 400)
}

suspend fun scrollDown(): Boolean {
    val mtop = (ScreenAdapter.relHeight * 0.33).toInt()
    val mBottom = (ScreenAdapter.relHeight * 0.9).toInt()
    val xCenter = (ScreenAdapter.relWidth * 0.5).toInt()
    return swipe(xCenter, mtop, xCenter, mBottom, 400)
}


suspend fun swipeDirection(
    startX: Int, startY: Int,
    direction: Int, distance: Int,
    duration: Long
): Boolean {
    var endX = startX
    var endY = startY

    when (direction) {
        // 上滑
        -1 -> endY -= distance
        // 下滑
        1 -> endY += distance
        // 左滑
        -2 -> endX -= distance
        // 右滑
        2 -> endX += distance
        else -> throw IllegalArgumentException("Invalid direction. Use -1 for up, 1 for down, -2 for left, 2 for right.")
    }

    return gesture(duration, arrayOf(Pair(startX, startY), Pair(endX, endY)))
}