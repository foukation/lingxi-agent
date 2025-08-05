package com.fxzs.lingxiagent.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Log;

import java.security.MessageDigest;

/**
 * 签名工具类
 * 用于获取应用签名信息
 */
public class SignatureUtil {
    private static final String TAG = "SignatureUtil";
    
    /**
     * 获取应用签名信息
     */
    public static void logSignatureInfo(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            
            Signature[] signatures = packageInfo.signatures;
            if (signatures != null && signatures.length > 0) {
                Signature signature = signatures[0];
                
                // 获取MD5签名
                String md5 = getSignatureMD5(signature);
                Log.d(TAG, "应用签名MD5: " + md5);
                Log.d(TAG, "应用签名MD5(去冒号): " + md5.replace(":", ""));
                
                // 获取SHA1签名
                String sha1 = getSignatureSHA1(signature);
                Log.d(TAG, "应用签名SHA1: " + sha1);
                
                // 获取SHA256签名
                String sha256 = getSignatureSHA256(signature);
                Log.d(TAG, "应用签名SHA256: " + sha256);
            }
        } catch (Exception e) {
            Log.e(TAG, "获取签名信息失败", e);
        }
    }
    
    /**
     * 获取签名的MD5值
     */
    private static String getSignatureMD5(Signature signature) {
        return getSignatureHash(signature, "MD5");
    }
    
    /**
     * 获取签名的SHA1值
     */
    private static String getSignatureSHA1(Signature signature) {
        return getSignatureHash(signature, "SHA1");
    }
    
    /**
     * 获取签名的SHA256值
     */
    private static String getSignatureSHA256(Signature signature) {
        return getSignatureHash(signature, "SHA-256");
    }
    
    /**
     * 获取签名的哈希值
     */
    private static String getSignatureHash(Signature signature, String algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            md.update(signature.toByteArray());
            byte[] digest = md.digest();
            
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < digest.length; i++) {
                String hex = Integer.toHexString(0xFF & digest[i]);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
                if (i < digest.length - 1) {
                    sb.append(":");
                }
            }
            return sb.toString().toUpperCase();
        } catch (Exception e) {
            Log.e(TAG, "计算签名哈希失败", e);
            return "";
        }
    }
}