package com.fxzs.lingxiagent.util.ZUtil;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.widget.TextView;

import androidx.annotation.NonNull;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.target.Target;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.core.CorePlugin;
import io.noties.markwon.image.AsyncDrawable;
import io.noties.markwon.image.glide.GlideImagesPlugin;
import io.noties.markwon.recycler.table.TableEntryPlugin;
import io.noties.markwon.syntax.Prism4jThemeDefault;
import io.noties.markwon.syntax.SyntaxHighlightPlugin;
import io.noties.prism4j.Prism4j;
import org.jetbrains.annotations.NotNull;

public class MarkdownUtils {
    private static final String TAG = "MarkdownUtils";
    private static final String FONT_PATH = "fonts/jjyh.ttf";

    public static Markwon createMarkwon(Context context) {
        Markwon.Builder builder = Markwon.builder(context)
                .usePlugin(CorePlugin.create())
                .usePlugin(new ModernMarkdownThemePlugin(context)) // 使用现代化主题
                .usePlugin(TextAlignmentPlugin.create(context)) // 使用文本对齐插件
                .usePlugin(GlideImagesPlugin.create(context))
                .usePlugin(GlideImagesPlugin.create(Glide.with(context)))
                .usePlugin(GlideImagesPlugin.create(new GlideImagesPlugin.GlideStore() {
                    @Override
                    public void cancel(Target<?> target) {
                        Glide.with(context).clear(target);
                    }

                    @Override
                    public RequestBuilder<Drawable> load(AsyncDrawable drawable) {
                        return Glide.with(context).load(drawable.getDestination());
                    }
                }))
                .textSetter(new Markwon.TextSetter() {
                    @Override
                    public void setText(@NonNull @NotNull TextView textView, @NonNull @NotNull Spanned spanned, @NonNull @NotNull TextView.BufferType bufferType, @NonNull @NotNull Runnable runnable) {
                        // 字体设置现在在 ModernMarkdownThemePlugin 中通过 Span 处理
                        // 设置自定义字体，同时保留样式（如加粗）
//                        android.graphics.Typeface customTypeface = FontCacheManager.getInstance().getTypeface(context, "fonts/jjyh.ttf");
//
//                        // 设置字体
//                        textView.setTypeface(customTypeface);

                        // 这样可以保留加粗等样式效果
                        textView.setTextSize(14);

                        // 设置合适的行间距，避免标题换行时行间距过大
                        textView.setLineSpacing(0, 1.2f); // 1.2倍行间距

                        // 增加TextView的内边距，改善整体间距效果
                        int padding = (int) (4 * context.getResources().getDisplayMetrics().density);
                        textView.setPadding(textView.getPaddingLeft(), padding,
                                          textView.getPaddingRight(), padding);

                        // 设置文本对齐方式：正文内容平铺整个容器
                        TextAlignmentHelper.setBodyAlignment(textView);

                        // 设置文本内容
                        textView.setText(spanned);
                    }
                })
                .usePlugin(TableEntryPlugin.create(builder1 -> builder1
                        .tableHeaderRowBackgroundColor(0xDAD5DADC)
                        .tableBorderWidth(0)))
                ;

        // 尝试添加语法高亮，如果失败则忽略
        try {
            android.util.Log.d(TAG, "createMarkwon: Attempting to add syntax highlighting");
            final Prism4j prism4j = new Prism4j(SimplePrismGrammarLocator.getInstance());
            builder.usePlugin(SyntaxHighlightPlugin.create(prism4j, Prism4jThemeDefault.create()));
            android.util.Log.d(TAG, "createMarkwon: Syntax highlighting added successfully");
        } catch (Exception e) {
            // 如果语法高亮失败，继续使用基础功能
            android.util.Log.w(TAG, "Failed to initialize syntax highlighting: " + e.getMessage());
        }

        android.util.Log.d(TAG, "createMarkwon: Building final Markwon instance");
        return builder.build();
    }

    /**
     * 创建优化的Markdown渲染器
     */
    public static MarkdownRenderer createRenderer(Context context) {
        return new MarkdownRenderer(context);
    }

    /**
     * 便捷方法：智能渲染Markdown到TextView
     */
    public static void renderSmart(Context context, String markdown, TextView textView) {
        MarkdownRenderer renderer = createRenderer(context);
        if (markdown != null && markdown.length() > 1000) {
            renderer.renderLargeMarkdown(markdown, textView, null);
        } else {
            renderer.renderWithCache(markdown, textView);
        }
    }

    /**
     * 预加载字体，建议在应用启动时调用
     * @param context 应用上下文
     */
    public static void preloadFonts(Context context) {
        FontCacheManager.getInstance().preloadFont(context, FONT_PATH);
        android.util.Log.d(TAG, "Font preloading initiated");
    }

    /**
     * 清理字体缓存，建议在内存紧张时调用
     */
    public static void clearFontCache() {
        FontCacheManager.getInstance().clearCache();
        android.util.Log.d(TAG, "Font cache cleared");
    }

    /**
     * 验证自定义字体是否正确加载
     * @param context 应用上下文
     */
    public static void verifyCustomFont(Context context) {
        android.util.Log.d(TAG, "=== Custom Font Verification ===");

        // 检查字体是否在缓存中
        FontCacheManager fontManager = FontCacheManager.getInstance();
        boolean isCached = fontManager.isFontCached(FONT_PATH);
        android.util.Log.d(TAG, "Font cached: " + isCached);

        // 尝试加载字体
        Typeface customFont = fontManager.getTypeface(context, FONT_PATH);
        android.util.Log.d(TAG, "Font loaded: " + (customFont != null));
        android.util.Log.d(TAG, "Is default font: " + (customFont == Typeface.DEFAULT));

        if (customFont != null && customFont != Typeface.DEFAULT) {
            android.util.Log.d(TAG, "✅ Custom font is working!");
        } else {
            android.util.Log.w(TAG, "❌ Custom font may not be working properly");
        }

        android.util.Log.d(TAG, "================================");
    }
}
