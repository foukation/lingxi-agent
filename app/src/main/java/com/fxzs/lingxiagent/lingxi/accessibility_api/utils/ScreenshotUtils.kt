package cn.vove7.andro_accessibility_api.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.os.Build
import android.util.Base64
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.graphics.scale
import cn.vove7.andro_accessibility_api.BaseAccessibilityService
import com.fxzs.lingxiagent.lingxi.accessibility_api.utils.TaskPool
import timber.log.Timber
import java.io.ByteArrayOutputStream
import kotlin.math.min


object ScreenshotUtils {

    private var Tag : String = "ScreenshotUtils"

    @RequiresApi(Build.VERSION_CODES.R)
    fun getScreenshotBase64(context: Context, callback: (String?) -> Unit) {
        val screenSize =  getScreenSizeWithNavigationBar(context)
        TaskPool.CACHE.execute {
            var imageBase64: String? = null
            try {
                // 调用截图方法
                BaseAccessibilityService().takeScreenshotSec { result ->
                    if (result != null) {

                        val proportion = 1120.0/screenSize.y
                        val width = (screenSize.x * proportion).toInt()
                        Timber.tag(Tag).i("图片压缩尺寸: $proportion $width")
                        imageBase64 = getBase64FromBitmapSize(result,width,1120)
                        // 回收 Bitmap
                        result.recycle()

                        // 在主线程上回调结果
                        TaskPool.MAIN.post {
                            callback(imageBase64)
                        }
                    } else {
                        // 在主线程上回调结果
                        TaskPool.MAIN.post {
                            callback("截图失败")
                        }

                    }
                }
            } catch (e: Exception) {
                // 处理可能的异常（例如，截图失败、文件写入失败等）
                Timber.tag(Tag).i("截图失败: ${e.printStackTrace()}")
                e.printStackTrace()
            }
        }
    }

    fun getScreenSizeWithNavigationBar(context: Context): Point {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getRealSize(size)
        return size
    }

    fun getBase64FromBitmapSize(bitmap: Bitmap?, maxWidth: Int, maxHeight: Int): String? {

        // 检查位图是否为空
        bitmap ?: return null

        // 计算缩放比例
        val scaleWidth = maxWidth.toFloat() / bitmap.width.toFloat()
        val scaleHeight = maxHeight.toFloat() / bitmap.height.toFloat()
        val scale = min(scaleWidth, scaleHeight)

        // 创建缩放后的位图
        val scaledBitmap =
            bitmap.scale((bitmap.width * scale).toInt(), (bitmap.height * scale).toInt())

        // 使用 ByteArrayOutputStream 捕获压缩后的位图数据
        val byteArrayOutputStream = ByteArrayOutputStream()
        // 将缩放后的位图压缩为 PNG 格式，并写入 ByteArrayOutputStream（这里可以根据需要调整质量）
        scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)

        // 注意：如果需要更小的文件大小，可以考虑使用 JPEG 格式并降低质量
        // scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)

        // 获取压缩后的字节数组
        val imageBytes = byteArrayOutputStream.toByteArray()

        // 将字节数组转换为 Base64 编码的字符串，不使用换行符
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP)

    }

}