package com.fxzs.lingxiagent.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Word文档导出工具类（使用RTF格式）
 */
public class WordExportUtil {
    private static final String TAG = "WordExportUtil";

    /**
     * 导出AIResponse内容为Word文档（RTF格式）
     *
     * @param context 上下文
     * @param title 文档标题
     * @param content 内容（支持Markdown格式）
     * @param callback 导出结果回调
     */
    public static void exportToWord(Context context, String title, String content, ExportCallback callback) {
        new Thread(() -> {
            try {
                // 生成RTF内容
                StringBuilder rtfContent = new StringBuilder();

                // RTF文档头 - 使用UTF-8编码支持中文
                rtfContent.append("{\\rtf1\\ansi\\ansicpg936\\deff0\\deflang2052");
                rtfContent.append("{\\fonttbl{\\f0\\fnil\\fcharset134\\fprq2 SimSun;}}");
                rtfContent.append("{\\colortbl;\\red0\\green0\\blue0;\\red102\\green102\\blue102;}");
                rtfContent.append("\\viewkind4\\uc1\\pard");

                // 添加标题
                addRTFTitle(rtfContent, title);

                // 添加生成时间
                addRTFGenerationTime(rtfContent);

                // 添加分隔线
                addRTFSeparator(rtfContent);

                // 解析并添加内容
                parseAndAddRTFContent(rtfContent, content);

                // RTF文档尾
                rtfContent.append("}");

                // 保存文档
                File file = saveRTFDocument(context, rtfContent.toString(), title);

                // 回调成功
                if (callback != null) {
                    callback.onSuccess(file);
                }

            } catch (Exception e) {
                Log.e(TAG, "导出Word文档失败", e);
                if (callback != null) {
                    callback.onError("导出失败: " + e.getMessage());
                }
            }
        }).start();
    }
    
    /**
     * 添加RTF文档标题
     */
    private static void addRTFTitle(StringBuilder rtf, String title) {
        String escapedTitle = escapeRTFText(title);

        Log.d(TAG, "标题原文: " + title);
        Log.d(TAG, "标题转义: " + escapedTitle);

        rtf.append("\\pard\\qc\\f0\\fs36\\b ");
        rtf.append(escapedTitle);
        rtf.append("\\b0\\par\\par");
    }

    /**
     * 添加RTF生成时间
     */
    private static void addRTFGenerationTime(StringBuilder rtf) {
        String currentTime = new SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault()).format(new Date());
        String timeText = "生成时间: " + currentTime;
        String escapedTimeText = escapeRTFText(timeText);

        Log.d(TAG, "时间原文: " + timeText);
        Log.d(TAG, "时间转义: " + escapedTimeText);

        rtf.append("\\pard\\qr\\f0\\fs20\\cf2 ");
        rtf.append(escapedTimeText);
        rtf.append("\\cf1\\par\\par");
    }

    /**
     * 添加RTF分隔线
     */
    private static void addRTFSeparator(StringBuilder rtf) {
        rtf.append("\\pard\\qc\\f0\\fs20\\cf2 ");
        // 使用简单的ASCII字符作为分隔线，避免特殊字符乱码
        rtf.append("----------------------------------------");
        rtf.append("\\cf1\\par\\par");
    }

    /**
     * 转义RTF特殊字符并处理中文编码
     */
    private static String escapeRTFText(String text) {
        if (text == null) return "";

        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c == '\\') {
                result.append("\\\\");
            } else if (c == '{') {
                result.append("\\{");
            } else if (c == '}') {
                result.append("\\}");
            } else if (c == '\n') {
                result.append("\\par ");
            } else if (c > 127) {
                // 中文字符使用Unicode编码
                result.append("\\u").append((int) c).append("?");
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
    
    /**
     * 解析Markdown内容并添加到RTF文档
     */
    private static void parseAndAddRTFContent(StringBuilder rtf, String content) {
        if (content == null || content.trim().isEmpty()) {
            rtf.append("\\pard\\f0\\fs24 ");
            rtf.append(escapeRTFText("暂无内容"));
            rtf.append("\\par");
            return;
        }

        // 按行分割内容
        String[] lines = content.split("\n");

        for (String line : lines) {
            line = line.trim();

            if (line.isEmpty()) {
                // 空行
                rtf.append("\\par");
                continue;
            }

            if (line.startsWith("# ")) {
                // 一级标题
                addRTFHeading(rtf, line.substring(2), 32, true);
            } else if (line.startsWith("## ")) {
                // 二级标题
                addRTFHeading(rtf, line.substring(3), 28, true);
            } else if (line.startsWith("### ")) {
                // 三级标题
                addRTFHeading(rtf, line.substring(4), 24, true);
            } else if (line.startsWith("- ") || line.startsWith("* ")) {
                // 无序列表
                addRTFBulletPoint(rtf, line.substring(2));
            } else if (line.matches("^\\d+\\. .*")) {
                // 有序列表
                addRTFNumberedPoint(rtf, line);
            } else {
                // 普通段落
                addRTFFormattedText(rtf, line);
            }
        }
    }
    
    /**
     * 添加RTF标题
     */
    private static void addRTFHeading(StringBuilder rtf, String text, int fontSize, boolean bold) {
        rtf.append("\\pard\\f0\\fs").append(fontSize);
        if (bold) {
            rtf.append("\\b ");
        }
        rtf.append(escapeRTFText(text));
        if (bold) {
            rtf.append("\\b0");
        }
        rtf.append("\\par\\par");
    }

    /**
     * 添加RTF无序列表项
     */
    private static void addRTFBulletPoint(StringBuilder rtf, String text) {
        rtf.append("\\pard\\li720\\f0\\fs24 ");
        rtf.append("• ");
        addRTFFormattedText(rtf, text);
        rtf.append("\\par");
    }

    /**
     * 添加RTF有序列表项
     */
    private static void addRTFNumberedPoint(StringBuilder rtf, String text) {
        rtf.append("\\pard\\li720\\f0\\fs24 ");
        addRTFFormattedText(rtf, text);
        rtf.append("\\par");
    }
    
    /**
     * 添加RTF格式化文本（支持粗体等）
     */
    private static void addRTFFormattedText(StringBuilder rtf, String text) {
        if (text == null || text.trim().isEmpty()) {
            rtf.append("\\pard\\f0\\fs24 \\par");
            return;
        }

        // 处理粗体 **text**
        Pattern boldPattern = Pattern.compile("\\*\\*(.*?)\\*\\*");
        Matcher boldMatcher = boldPattern.matcher(text);

        rtf.append("\\pard\\f0\\fs24 ");

        int lastEnd = 0;
        boolean hasFormatting = false;

        while (boldMatcher.find()) {
            hasFormatting = true;

            // 添加粗体前的普通文本
            if (boldMatcher.start() > lastEnd) {
                rtf.append(escapeRTFText(text.substring(lastEnd, boldMatcher.start())));
            }

            // 添加粗体文本
            rtf.append("\\b ");
            rtf.append(escapeRTFText(boldMatcher.group(1)));
            rtf.append("\\b0 ");

            lastEnd = boldMatcher.end();
        }

        // 添加剩余的普通文本
        if (lastEnd < text.length()) {
            rtf.append(escapeRTFText(text.substring(lastEnd)));
        }

        // 如果没有找到任何格式化标记，直接添加普通文本
        if (!hasFormatting) {
            rtf.append(escapeRTFText(text));
        }

        rtf.append("\\par");
    }
    
    /**
     * 保存RTF文档到文件
     */
    private static File saveRTFDocument(Context context, String rtfContent, String title) throws IOException {
        // 创建文件名
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = sanitizeFileName(title) + "_" + timestamp + ".rtf";

        // 获取外部存储目录
        File documentsDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "exports");
        if (!documentsDir.exists()) {
            documentsDir.mkdirs();
        }

        File file = new File(documentsDir, fileName);

        // 写入RTF文件 - 直接写入字节以确保编码正确
        try (FileOutputStream fos = new FileOutputStream(file)) {
            // RTF文档应该使用ANSI编码，但我们已经在内容中使用了Unicode转义
            fos.write(rtfContent.getBytes(StandardCharsets.UTF_8));
        }

        Log.d(TAG, "RTF文档已保存到: " + file.getAbsolutePath());
        return file;
    }
    
    /**
     * 清理文件名中的非法字符
     */
    private static String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "document";
        }
        
        // 移除或替换非法字符
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_")
                      .replaceAll("\\s+", "_")
                      .substring(0, Math.min(fileName.length(), 50));
    }
    
    /**
     * 打开导出的文档
     */
    public static void openDocument(Context context, File file) {
        try {
            Uri uri = FileProvider.getUriForFile(context,
                context.getPackageName() + ".fileprovider", file);

            Intent intent = new Intent(Intent.ACTION_VIEW);

            // 根据文件扩展名设置MIME类型
            String fileName = file.getName().toLowerCase();
            if (fileName.endsWith(".rtf")) {
                intent.setDataAndType(uri, "application/rtf");
            } else if (fileName.endsWith(".docx")) {
                intent.setDataAndType(uri, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            } else {
                intent.setDataAndType(uri, "text/plain");
            }

            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(Intent.createChooser(intent, "打开文档"));
        } catch (Exception e) {
            Log.e(TAG, "打开文档失败", e);
        }
    }
    
    /**
     * 测试中文编码
     */
    public static void testChineseEncoding() {
        String testText = "测试中文编码：会议摘要、话题讨论、智能问答";
        String escaped = escapeRTFText(testText);
        Log.d(TAG, "原文: " + testText);
        Log.d(TAG, "转义后: " + escaped);
    }

    /**
     * 导出结果回调接口
     */
    public interface ExportCallback {
        void onSuccess(File file);
        void onError(String error);
    }
}
