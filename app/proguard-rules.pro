# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#避免混淆一键登录SDK
-dontwarn com.cmic.sso.sdk.**
-keep class com.cmic.sso.sdk.** {*;}
# OkHttp 3.x 及以上版本混淆规则
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.squareup.okhttp3.** { *; }
-keep interface com.squareup.okhttp3.** { *; }
-dontwarn com.squareup.okhttp3.**

# OkHttp 依赖的 Okio 库
-keep class com.squareup.okio.** { *; }
-keep interface com.squareup.okio.** { *; }
-dontwarn com.squareup.okio.**

# 如果使用了 Retrofit（常与 OkHttp 配合使用），也需要保留
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-dontwarn retrofit2.**
-keepattributes Exceptions

# 保留 Retrofit 接口和注解（如果用到）
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
# 保留所有 Activity 类及其构造方法
-keep public class * extends android.app.Activity {
    public <init>();
}

# 保留 Service、BroadcastReceiver、ContentProvider
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.Application

# 保留 Fragment（若使用）
-keep public class * extends android.app.Fragment
# 保留注解
-keepattributes *Annotation*
-keepattributes Signature

# 保留资源相关类（如 R 文件）
-keep class **.R$* {
    public static <fields>;
}

-keep class com.fxzs.lingxiagent.model.** { *; }
-keep class com.fxzs.lingxiagent.viewmodel.** { *; }
-keep class com.fxzs.lingxiagent.view.** { *; }
-keep class com.fxzs.lingxiagent.network.** { *; }
# 兜底：保留所有 Activity 子类（防止遗漏其他页面）
-keep public class * extends android.app.Activity {
    public <init>();
    public void onCreate(android.os.Bundle);
    public void startActivity(android.content.Intent); # 保留启动 Activity 的关键方法
    public void startActivity(android.content.Intent, android.os.Bundle);
}
-keep public class * extends androidx.appcompat.app.AppCompatActivity {
     public <init>();
     public void onCreate(android.os.Bundle);
     public void startActivity(android.content.Intent); # 保留启动 Activity 的关键方法
     public void startActivity(android.content.Intent, android.os.Bundle);
 }
 # 保留 Gson 相关的类型信息（关键）
 -keepattributes Signature
 -keepattributes *Annotation*
 -keep class com.google.gson.** { *; }
 -dontwarn com.google.gson.**
 # 保留抽象类（日志中提到的 com.cmdc.ai.assist.constraint.d）
 # 注意：若类名被混淆，需根据 mapping.txt 还原真实类名
 -keep public abstract class com.cmdc.ai.assist.constraint.* {
     <fields>;
     <methods>;
 }

 # 保留其所有实现类
 -keep public class * extends com.cmdc.ai.assist.constraint.* {
     public <init>(); # 保留无参构造，Gson 需要通过反射创建实例
     <fields>;
     <methods>;
 }
 # 保留整个包下的所有类（包括子包），不进行混淆
 -keep class com.cmdc.ai.assist.*.** {
     *; # 保留所有字段和方法
 }
 # 对实现 Serializable 的类，保留 serialVersionUID 字段
 -keepclassmembers class * implements java.io.Serializable {
     static final long serialVersionUID;
     private static final java.io.ObjectStreamField[] serialPersistentFields;
     private void writeObject(java.io.ObjectOutputStream);
     private void readObject(java.io.ObjectInputStream);
     java.lang.Object writeReplace();
     java.lang.Object readResolve();
 }

 -keep public class * extends androidx.fragment.app.Fragment {
     public <init>();
 }

-keepclassmembers class * extends androidx.fragment.app.Fragment {
    public void onCreate(android.os.Bundle);
    public void onViewCreated(android.view.View, android.os.Bundle);
    public void onDestroyView();
}
-keep public class com.fxzs.lingxiagent.view.chat.SuperChatFragment {
    public <init>();
    <fields>;
    <methods>;
}