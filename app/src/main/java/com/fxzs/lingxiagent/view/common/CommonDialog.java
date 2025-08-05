package com.fxzs.lingxiagent.view.common;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.fxzs.lingxiagent.R;

/**
 * 通用弹窗组件
 * 支持自定义标题、内容、按钮文字和样式
 */
public class CommonDialog {
    
    public interface OnDialogClickListener {
        void onConfirm();
        void onCancel();
    }
    
    public static class Builder {
        private Context context;
        private String title = "提示";
        private String message = "";
        private boolean isHtmlMessage = false;
        private String confirmText = "确认";
        private String cancelText = "取消";
        private boolean confirmTextRed = false;
        private OnDialogClickListener listener;
        private boolean cancelable = true;
        private int cancelBtnVisible;

        public Builder(Context context) {
            this.context = context;
        }
        
        /**
         * 设置标题
         */
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }
        
        /**
         * 设置内容消息
         */
        public Builder setMessage(String message) {
            this.message = message;
            this.isHtmlMessage = false;
            return this;
        }

        /**
         * 设置HTML格式的内容消息
         */
        public Builder setHtmlMessage(String htmlMessage) {
            this.message = htmlMessage;
            this.isHtmlMessage = true;
            return this;
        }
        
        /**
         * 设置确认按钮文字
         */
        public Builder setConfirmText(String confirmText) {
            this.confirmText = confirmText;
            return this;
        }

        public Builder setCancelVisible(int visible) {
            this.cancelBtnVisible = visible;
            return this;
        }

        /**
         * 设置取消按钮文字
         */
        public Builder setCancelText(String cancelText) {
            this.cancelText = cancelText;
            return this;
        }
        
        /**
         * 设置确认按钮是否为红色（警告样式）
         */
        public Builder setConfirmTextRed(boolean isRed) {
            this.confirmTextRed = isRed;
            return this;
        }
        
        /**
         * 设置点击监听器
         */
        public Builder setOnClickListener(OnDialogClickListener listener) {
            this.listener = listener;
            return this;
        }
        
        /**
         * 设置是否可取消
         */
        public Builder setCancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }
        
        /**
         * 创建并显示弹窗
         */
        public Dialog show() {
            Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.dialog_common);
            
            // 设置窗口背景为透明
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                dialog.getWindow().setAttributes(params);
            }
            
            // 初始化控件
            TextView tvTitle = dialog.findViewById(R.id.tv_title);
            TextView tvMessage = dialog.findViewById(R.id.tv_message);
            TextView btnCancel = dialog.findViewById(R.id.btn_cancel);
            TextView btnConfirm = dialog.findViewById(R.id.btn_confirm);
            View middle_line = dialog.findViewById(R.id.middle_line);

            // 设置内容
            if (tvTitle != null) tvTitle.setText(title);
            if (tvMessage != null) {
                if (isHtmlMessage) {
                    tvMessage.setText(Html.fromHtml(message));
                    tvMessage.setMovementMethod(LinkMovementMethod.getInstance());
                } else {
                    tvMessage.setText(message);
                }
            }
            btnCancel.setVisibility(cancelBtnVisible);
            middle_line.setVisibility(cancelBtnVisible);
            if (btnCancel != null) btnCancel.setText(cancelText);
            if (btnConfirm != null) btnConfirm.setText(confirmText);
            
            // 设置确认按钮颜色
            if (btnConfirm != null) {
                if (confirmTextRed) {
                    btnConfirm.setTextColor(Color.parseColor("#FF4444"));
                } else {
                    btnConfirm.setTextColor(Color.parseColor("#1C77FF"));
                }
            }
            
            // 设置点击事件
            if (btnCancel != null) {
                btnCancel.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onCancel();
                    }
                    dialog.dismiss();
                });
            }
            
            if (btnConfirm != null) {
                btnConfirm.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onConfirm();
                    }
                    dialog.dismiss();
                });
            }
            
            dialog.setCancelable(cancelable);
            dialog.setCanceledOnTouchOutside(cancelable);
            dialog.show();
            
            return dialog;
        }
    }
    
    /**
     * 快速创建注销账号确认弹窗
     */
    public static Dialog showAccountDeletionDialog(Context context, OnDialogClickListener listener) {
        return new Builder(context)
                .setTitle("安全提示")
                .setMessage("注销账号后，将不会保留当前账号的所有数据，请谨慎操作")
                .setConfirmText("确认注销")
                .setConfirmTextRed(true)
                .setCancelText("取消")
                .setOnClickListener(listener)
                .show();
    }
    
    /**
     * 快速创建退出登录确认弹窗
     */
    public static Dialog showLogoutDialog(Context context, OnDialogClickListener listener) {
        return new Builder(context)
                .setTitle("退出登录")
                .setMessage("确定要退出当前账号吗？")
                .setConfirmText("退出")
                .setConfirmTextRed(false)
                .setCancelText("取消")
                .setOnClickListener(listener)
                .show();
    }
    
    /**
     * 快速创建通用确认弹窗
     */
    public static Dialog showConfirmDialog(Context context, String title, String message,
                                         String confirmText, OnDialogClickListener listener) {
        return new Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setConfirmText(confirmText)
                .setOnClickListener(listener)
                .show();
    }
    public static Dialog showConfirmOneBtnDialog(Context context, String title, String message,
                                         String confirmText, OnDialogClickListener listener) {
        return new Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelVisible(View.GONE)
                .setConfirmText(confirmText)
                .setOnClickListener(listener)
                .show();
    }

    /**
     * 快速创建支持HTML内容的确认弹窗
     */
    public static Dialog showHtmlConfirmDialog(Context context, String title, String htmlMessage,
                                             String confirmText, OnDialogClickListener listener) {
        return new Builder(context)
                .setTitle(title)
                .setHtmlMessage(htmlMessage)
                .setConfirmText(confirmText)
                .setOnClickListener(listener)
                .show();
    }

    /**
     * 快速创建协议确认弹窗（带有可点击的服务协议和隐私政策链接）
     */
    public static Dialog showAgreementDialog(Context context, OnDialogClickListener listener) {
        // 创建可点击的文本
        String text = "已阅读并同意服务协议和隐私政策";
        SpannableString spannableString = new SpannableString(text);

        // 服务协议点击事件
        ClickableSpan serviceSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                // 跳转到服务协议页面
                WebViewActivity.start(context, "https://mobile-web.jmkjsh.com/user_contract.html", "服务协议");
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.parseColor("#1C77FF"));
                ds.setUnderlineText(true);
            }
        };

        // 隐私政策点击事件
        ClickableSpan privacySpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                // 跳转到隐私政策页面
                WebViewActivity.start(context, "https://mobile-web.jmkjsh.com/privacy.html", "隐私政策");
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.parseColor("#1C77FF"));
                ds.setUnderlineText(true);
            }
        };

        // 设置点击范围
        int serviceStart = text.indexOf("服务协议");
        int serviceEnd = serviceStart + "服务协议".length();
        int privacyStart = text.indexOf("隐私政策");
        int privacyEnd = privacyStart + "隐私政策".length();

        spannableString.setSpan(serviceSpan, serviceStart, serviceEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(privacySpan, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // 创建弹窗
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_common);

        // 设置窗口背景为透明
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setAttributes(params);
        }

        // 初始化控件
        TextView tvTitle = dialog.findViewById(R.id.tv_title);
        TextView tvMessage = dialog.findViewById(R.id.tv_message);
        TextView btnCancel = dialog.findViewById(R.id.btn_cancel);
        TextView btnConfirm = dialog.findViewById(R.id.btn_confirm);

        // 设置内容
        if (tvTitle != null) tvTitle.setText("使用协议及隐私保护");
        if (tvMessage != null) {
            tvMessage.setText(spannableString);
            tvMessage.setMovementMethod(LinkMovementMethod.getInstance());
        }
        if (btnCancel != null) btnCancel.setText("取消");
        if (btnConfirm != null) {
            btnConfirm.setText("同意");
            btnConfirm.setTextColor(Color.parseColor("#1C77FF"));
        }

        // 设置点击事件
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> {
                dialog.dismiss();
                if (listener != null) listener.onCancel();
            });
        }

        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                dialog.dismiss();
                if (listener != null) listener.onConfirm();
            });
        }

        dialog.show();
        return dialog;
    }
    
    /**
     * 快速创建警告弹窗（红色确认按钮）
     */
    public static Dialog showWarningDialog(Context context, String title, String message, 
                                         String confirmText, OnDialogClickListener listener) {
        return new Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setConfirmText(confirmText)
                .setConfirmTextRed(true)
                .setOnClickListener(listener)
                .show();
    }
}
