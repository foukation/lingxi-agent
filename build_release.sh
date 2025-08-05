#!/bin/bash

# 脚本说明：构建Release版本APK
# 使用方法：./build_release.sh

echo "=========================================="
echo "开始构建Release版本APK"
echo "=========================================="

# 清理之前的构建
echo "1. 清理旧的构建文件..."
./gradlew clean

# 构建Release APK
echo "2. 构建Release APK..."
./gradlew assembleRelease

# 检查构建是否成功
if [ $? -ne 0 ]; then
    echo "❌ 构建失败！请检查错误信息。"
    exit 1
fi

echo "✅ 构建成功！"

# APK路径
APK_PATH="app/build/outputs/apk/release/app-release.apk"

# 检查APK是否存在
if [ ! -f "$APK_PATH" ]; then
    echo "❌ 找不到APK文件：$APK_PATH"
    exit 1
fi

# 显示APK信息
echo ""
echo "3. APK信息："
echo "文件路径: $APK_PATH"
echo "文件大小: $(du -h $APK_PATH | cut -f1)"
echo "构建时间: $(date)"

# 创建输出目录
OUTPUT_DIR="release"
mkdir -p $OUTPUT_DIR

# 生成带时间戳的文件名
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
VERSION_NAME=$(grep "versionName" app/build.gradle | awk -F'"' '{print $2}')
OUTPUT_FILENAME="灵犀_v${VERSION_NAME}_${TIMESTAMP}.apk"
OUTPUT_PATH="$OUTPUT_DIR/$OUTPUT_FILENAME"

# 复制APK到输出目录
echo ""
echo "4. 复制APK到输出目录..."
cp "$APK_PATH" "$OUTPUT_PATH"

if [ $? -eq 0 ]; then
    echo "✅ APK已复制到: $OUTPUT_PATH"
else
    echo "❌ 复制失败！"
    exit 1
fi

# 验证签名
echo ""
echo "5. 验证APK签名..."
echo "获取APK签名信息："
keytool -printcert -jarfile "$OUTPUT_PATH" | grep -E "(MD5|SHA1|SHA256):" | head -3

echo ""
echo "预期的Release签名："
echo "MD5:     F4:F0:3A:A7:B3:1F:AA:BF:F7:C2:9C:5D:2A:98:B9:10"
echo "SHA1:    91:15:CD:A9:BF:80:F0:C8:81:2F:21:09:48:8F:D6:41:5C:97:E0:96"
echo "SHA256:  CB:40:BF:89:F6:9A:D1:49:6D:C3:F4:32:DF:70:35:5A:0B:36:67:2B:56:12:F6:06:E9:8B:BE:4C:D5:1A:9E:D0"

# 生成文件列表
echo ""
echo "6. 生成发布文件列表..."
echo "Release APK 文件列表：" > "$OUTPUT_DIR/README.txt"
echo "========================" >> "$OUTPUT_DIR/README.txt"
echo "" >> "$OUTPUT_DIR/README.txt"
echo "文件名: $OUTPUT_FILENAME" >> "$OUTPUT_DIR/README.txt"
echo "大小: $(du -h $OUTPUT_PATH | cut -f1)" >> "$OUTPUT_DIR/README.txt"
echo "MD5: $(md5 -q $OUTPUT_PATH 2>/dev/null || md5sum $OUTPUT_PATH | awk '{print $1}')" >> "$OUTPUT_DIR/README.txt"
echo "构建时间: $(date)" >> "$OUTPUT_DIR/README.txt"
echo "版本号: $VERSION_NAME" >> "$OUTPUT_DIR/README.txt"
echo "" >> "$OUTPUT_DIR/README.txt"
echo "包名: com.fxzs.lingxiagent" >> "$OUTPUT_DIR/README.txt"
echo "签名: Release签名 (yidiong.jks)" >> "$OUTPUT_DIR/README.txt"

echo ""
echo "=========================================="
echo "✨ Release APK 构建完成！"
echo "=========================================="
echo ""
echo "输出文件："
echo "  APK: $OUTPUT_PATH"
echo "  说明: $OUTPUT_DIR/README.txt"
echo ""
echo "你可以："
echo "  1. 将 $OUTPUT_FILENAME 上传到应用商店"
echo "  2. 分享给测试人员进行测试"
echo "  3. 使用 adb install $OUTPUT_PATH 安装到设备"
echo ""