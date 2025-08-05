package com.fxzs.lingxiagent.util.ZUtil;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;
import android.util.Log;

/**
 * 内存监控工具类
 * 用于追踪内存使用情况，帮助定位内存相关的崩溃
 */
public class MemoryMonitor {
    private static final String TAG = "MemoryMonitor";
    
    /**
     * 打印当前内存使用情况
     */
    public static void logMemoryUsage(Context context, String location) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        
        // 获取应用的内存信息
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / 1024 / 1024; // MB
        long totalMemory = runtime.totalMemory() / 1024 / 1024; // MB
        long freeMemory = runtime.freeMemory() / 1024 / 1024; // MB
        long usedMemory = totalMemory - freeMemory;
        
        // 获取Native内存信息
        long nativeHeapSize = Debug.getNativeHeapSize() / 1024 / 1024; // MB
        long nativeHeapAllocated = Debug.getNativeHeapAllocatedSize() / 1024 / 1024; // MB
        long nativeHeapFree = Debug.getNativeHeapFreeSize() / 1024 / 1024; // MB
        
        Log.d(TAG, "=== Memory Usage at " + location + " ===");
        Log.d(TAG, "Max Memory: " + maxMemory + " MB");
        Log.d(TAG, "Total Memory: " + totalMemory + " MB");
        Log.d(TAG, "Used Memory: " + usedMemory + " MB");
        Log.d(TAG, "Free Memory: " + freeMemory + " MB");
        Log.d(TAG, "Memory Usage: " + (usedMemory * 100 / maxMemory) + "%");
        Log.d(TAG, "Native Heap Size: " + nativeHeapSize + " MB");
        Log.d(TAG, "Native Heap Allocated: " + nativeHeapAllocated + " MB");
        Log.d(TAG, "Native Heap Free: " + nativeHeapFree + " MB");
        Log.d(TAG, "System Low Memory: " + memoryInfo.lowMemory);
        Log.d(TAG, "Available System Memory: " + (memoryInfo.availMem / 1024 / 1024) + " MB");
        Log.d(TAG, "=====================================");
        
        // 如果内存使用超过80%，发出警告
        if (usedMemory * 100 / maxMemory > 80) {
            Log.w(TAG, "WARNING: Memory usage is high! Consider freeing resources.");
        }
        
        // 如果系统内存不足，发出错误
        if (memoryInfo.lowMemory) {
            Log.e(TAG, "ERROR: System is in low memory state!");
        }
    }
    
    /**
     * 强制垃圾回收并记录
     */
    public static void forceGC(String reason) {
        Log.d(TAG, "Forcing garbage collection: " + reason);
        System.gc();
        System.runFinalization();
        System.gc();
    }
}