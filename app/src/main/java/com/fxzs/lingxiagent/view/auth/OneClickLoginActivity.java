package com.fxzs.lingxiagent.view.auth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.cmic.sso.sdk.auth.TokenListener;
import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.model.auth.AuthHelper;
import com.fxzs.lingxiagent.model.common.Constants;
import com.fxzs.lingxiagent.view.common.BaseActivity;
import com.fxzs.lingxiagent.view.common.CommonDialog;
import com.fxzs.lingxiagent.view.common.GlobalToast;
import com.fxzs.lingxiagent.view.common.LoginPromptDialog;
import com.fxzs.lingxiagent.view.common.WebViewActivity;
import com.fxzs.lingxiagent.MainActivity;
import com.fxzs.lingxiagent.viewmodel.auth.VMRegister;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class OneClickLoginActivity extends BaseActivity<VMRegister> {

    private static final int REQUEST_PHONE_PERMISSION = 1001;
    private static final String phonePermission = Manifest.permission.READ_PHONE_STATE;
    private int tabIndex = -1;

    private ImageView btnBack;
    private TextView tvRegister;
    private TextView tvPhoneNumber;
    private Button btnOneClickLogin;
    private Button btnSwitchLoginMethod;
    private CheckBox cbAgreement;
    private TextView tvLoginAgree;
    private TextView tvCmccClause;
    private TextView tvLingxi;
    private TextView tvUserAgreement;
    private TextView tvPrivacyPolicy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化极光一键登录
//        initJVerification();

        // 预取号，提升用户体验
//        preLogin();

        // 检查是否从首页跳转过来
        checkIfFromHomePage();
    }
    
    @Override
    protected int getLayoutResource() {
        return R.layout.activity_one_click_login;
    }
    
    @Override
    protected Class<VMRegister> getViewModelClass() {
        return VMRegister.class;
    }
    
    @Override
    protected void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        tvRegister = findViewById(R.id.tvRegister);
//        tvPhoneNumber = findViewById(R.id.tvPhoneNumber);
        btnOneClickLogin = findViewById(R.id.btnOneClickLogin);
        btnSwitchLoginMethod = findViewById(R.id.btnSwitchLoginMethod);
        cbAgreement = findViewById(R.id.cbAgreement);
        tvLoginAgree = findViewById(R.id.tv_login_agree);
        tvCmccClause = findViewById(R.id.tv_cmcc_clause);
        tvLingxi = findViewById(R.id.tv_lingxi);
        tvUserAgreement = findViewById(R.id.tvUserAgreement);
        tvPrivacyPolicy = findViewById(R.id.tvPrivacyPolicy);

        // 检查并请求权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_PHONE_STATE},
                REQUEST_PHONE_PERMISSION);
        } else {
            displayPhoneNumberOrOperator();
        }

        // 设置点击事件（添加null检查）
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (tvRegister != null) {
            tvRegister.setOnClickListener(v -> {
                Intent intent = new Intent(this, RegisterNewActivity.class);
                startActivity(intent);
            });
        }

        if (btnOneClickLogin != null) {
            btnOneClickLogin.setOnClickListener(v -> performOneClickLogin());
        }

        if (btnSwitchLoginMethod != null) {
            btnSwitchLoginMethod.setOnClickListener(v -> {
                Intent intent = new Intent(this, RegisterActivity.class);
                startActivity(intent);
            });
        }

        if (tvUserAgreement != null) {
            tvUserAgreement.setOnClickListener(v -> {
                WebViewActivity.start(this, Constants.USER_AGREEMENT_URL, "用户协议");
            });
        }

        if (tvPrivacyPolicy != null) {
            tvPrivacyPolicy.setOnClickListener(v -> {
                WebViewActivity.start(this, Constants.PRIVACY_POLICY_URL, "隐私政策");
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // 取电话号码掩码，提升用户体验
        tryUmcLoginPre();
    }

    @Override
    protected void setupDataBinding() {
        // 暂无需要绑定的数据
    }
    
    @Override
    protected void setupObservers() {
        // 观察登录结果
        viewModel.getRegisterResult().observe(this, success -> {
            if (success != null && success) {
                // 登录成功，跳转到主页
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                if (tabIndex != -1) {
                    intent.putExtra("selected_tab", tabIndex);
                }
                startActivity(intent);
                finish();
            }
        });

        // 观察错误信息
        viewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                showToast(error);
            }
        });

        // 观察加载状态
        viewModel.getLoading().observe(this, loading -> {
            if (btnOneClickLogin != null) {
                btnOneClickLogin.setEnabled(!loading);
                if (loading) {
                    btnOneClickLogin.setText("登录中...");
                } else {
                    btnOneClickLogin.setText("一键登录");
                }
            }
        });
    }
    
//    private void initJVerification() {
//        JVerificationInterface.init(this);
//    }

//    private void preLogin() {
//        Log.d("OneClickLogin", "Starting preLogin");
//        JVerificationInterface.preLogin(this, 5000, new PreLoginListener() {
//            @Override
//            public void onResult(int code, String content, JSONObject jsonObject) {
//                Log.d("OneClickLogin", "preLogin onResult: code=" + code + ", content=" + content);
//                if (code == 7000) {
//                    // 预取号成功
//                    runOnUiThread(() -> {
//                        if (btnOneClickLogin != null) {
//                            btnOneClickLogin.setEnabled(true);
//                            Log.d("OneClickLogin", "预取号成功，一键登录按钮已启用");
//                        }
//                    });
//                } else {
//                    // 预取号失败，隐藏一键登录按钮或显示其他登录方式
//                    runOnUiThread(() -> {
//                        if (btnOneClickLogin != null) {
//                            btnOneClickLogin.setEnabled(false);
//                        }
//                        String errorMsg = "当前网络环境不支持一键登录";
//                        if (code == 2017) {
//                            errorMsg = "请关闭WiFi，使用移动数据网络";
//                        } else if (code == 2016) {
//                            errorMsg = "当前运营商暂不支持一键登录";
//                        }
//                        showToast(errorMsg);
//                        if (tvPhoneNumber != null) {
//                            tvPhoneNumber.setText(errorMsg);
//                        }
//                    });
//                }
//            }
//        });
//    }

    private void performOneClickLogin() {
        Log.d("OneClickLogin", "performOneClickLogin called");
        // 检查并请求权限
        if (ContextCompat.checkSelfPermission(this, phonePermission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, phonePermission)) {
                ActivityCompat.requestPermissions(this, new String[]{phonePermission}, REQUEST_PHONE_PERMISSION);
            } else {
                GlobalToast.show(this, "预取号码失败，请授予获取本机号码权限", GlobalToast.Type.ERROR);
            }
            return;
        }

        // 检查必要的UI组件是否存在
        if (cbAgreement == null) {
            Log.e("OneClickLogin", "cbAgreement is null");
            showToast("页面初始化失败，请重试");
            return;
        }

        // 检查是否同意协议
        if (!cbAgreement.isChecked()) {
            // 使用协议确认弹窗（带有可点击的链接）
            CommonDialog.showAgreementDialog(this,
                new CommonDialog.OnDialogClickListener() {
                    @Override
                    public void onConfirm() {
                        cbAgreement.setChecked(true);
                        performOneClickLogin();
                    }

                    @Override
                    public void onCancel() {
                        // 用户点击不同意，不做任何操作
                    }
                });
            return;
        }
//
//        // ====== 新增：设置极光一键登录自定义UI ======
//        JVerifyUIConfig uiConfig = new JVerifyUIConfig.Builder()
//                // 设置logo图片（假设已将网络图片下载为ic_app_logo.png）
//                .setLogoImgPath("ic_app_jg_logo")
//                .setLogoWidth(96) // dp，按你页面logo宽度
//                .setLogoHeight(96) // dp，按你页面logo高度
//                .setLogoOffsetY(80) // 距顶部距离，可根据实际调整
//
//                .setStatusBarTransparent(true)
//                .setStatusBarDarkMode(true)
//                .setNavTextColor(0xffffffff)
//                .setNavColor(0xffffffff)
//
//                .setNavReturnImgPath("login_back")
//                .setNavReturnBtnWidth(24)
//                .setNavReturnBtnHeight(24)
//
//                // 号码栏
//                .setNumberColor(0xFF222328) // 统一使用这个颜色
//                .setNumberSize(24)
//
//                // 登录按钮样式
//                .setLogBtnText("本机号码一键登录")
//                .setLogBtnTextColor(0xFFFFFFFF)
//                .setLogBtnImgPath("bg_btn_one_click_login")
//                .setLogBtnTextSize(16)
//                .setLogBtnWidth(313)
//                .setLogBtnHeight(48)
//                .setLogBtnOffsetY(280) // 距顶部距离，可根据实际调整
//                .setPrivacyState(true)
//                .setPrivacyNameAndUrlBeanList(new ArrayList<PrivacyBean>() {{
//                    add(new PrivacyBean("使用协议", "https://mobile-web.jmkjsh.com/user_contract.html", "，灵犀的"));
//                    add(new PrivacyBean("隐私政策", "https://mobile-web.jmkjsh.com/privacy.html","和"));
//                }})
//                .setAppPrivacyColor(0xFFBBBBBB, 0xFF1C77FF)
//                .setPrivacyTextSize(12)
//                .setPrivacyOffsetY(20)
//                .setPrivacyTextCenterGravity(true)
//                .setPrivacyCheckboxHidden(false)
//                .setPrivacyMarginT(350)
//                .setPrivacyMarginL(48)
//                .setPrivacyMarginR(48)
//                .setPrivacyCheckboxSize(16)
//                .setCheckedImgPath("login_checked")
//                .setUncheckedImgPath("login_check")
//                .build();
//        JVerificationInterface.setCustomUIWithConfig(uiConfig);
//        // ====== 新增结束 ======
//
//        // 配置登录设置
//        LoginSettings settings = new LoginSettings();
//        settings.setAutoFinish(true);
//        settings.setTimeout(15 * 1000);
//        settings.setAuthPageEventListener(new cn.jiguang.verifysdk.api.AuthPageEventListener() {
//            @Override
//            public void onEvent(int cmd, String msg) {
//                // 处理授权页面事件
//                Log.d("OneClickLogin", "AuthPageEvent: cmd=" + cmd + ", msg=" + msg);
//            }
//        });

        // 执行一键登录
        Log.d("OneClickLogin", "Calling loginAuth");
        AuthHelper.getInstance().getTokenImp(new AuthListener(2));
    }

    // 检查权限，预取手机号码
    private String tryUmcLoginPre() {
        // 检查并请求权限
        if (ContextCompat.checkSelfPermission(this, phonePermission) == PackageManager.PERMISSION_GRANTED) {
            AuthHelper.getInstance().umcLoginPre(new AuthListener(1));
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, phonePermission)) {
            ActivityCompat.requestPermissions(this, new String[]{phonePermission}, REQUEST_PHONE_PERMISSION);
        } else {
            GlobalToast.show(this, "预取号码失败，请授予获取本机号码权限", GlobalToast.Type.ERROR);
        }

//        try {
//            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//            if (tm == null) return null;
//
//            // 方法1：标准方法
//            String number = tm.getLine1Number();
//            if (number != null && !number.isEmpty()) {
//                Log.d("OneClickLogin", "方法1 getLine1Number: " + number);
//                return formatPhoneNumber(number);
//            }
//
//            // 方法2：通过SubscriptionManager（双卡手机）
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
//                try {
//                    android.telephony.SubscriptionManager sm = android.telephony.SubscriptionManager.from(this);
//                    List<android.telephony.SubscriptionInfo> subList = sm.getActiveSubscriptionInfoList();
//                    if (subList != null) {
//                        for (android.telephony.SubscriptionInfo info : subList) {
//                            String phoneNumber = info.getNumber();
//                            if (phoneNumber != null && !phoneNumber.isEmpty()) {
//                                Log.d("OneClickLogin", "方法2 SubscriptionInfo: " + phoneNumber);
//                                return formatPhoneNumber(phoneNumber);
//                            }
//                        }
//                    }
//                } catch (Exception e) {
//                    Log.e("OneClickLogin", "SubscriptionManager方法失败", e);
//                }
//            }
//
//            // 方法3：反射尝试获取
//            try {
//                Class<?> telephonyClass = Class.forName(tm.getClass().getName());
//                Method[] methods = telephonyClass.getMethods();
//                for (Method method : methods) {
//                    String methodName = method.getName();
//                    if (methodName.contains("getLine1Number") ||
//                        methodName.contains("getMsisdn") ||
//                        methodName.contains("getPhoneNumber")) {
//                        if (method.getParameterTypes().length == 0) {
//                            Object result = method.invoke(tm);
//                            if (result instanceof String && !((String)result).isEmpty()) {
//                                Log.d("OneClickLogin", "方法3 反射 " + methodName + ": " + result);
//                                return formatPhoneNumber((String)result);
//                            }
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                Log.e("OneClickLogin", "反射方法失败", e);
//            }
//
//            // 方法4：尝试从系统设置获取
//            try {
//                String phoneNumber = android.provider.Settings.System.getString(
//                    getContentResolver(), "phone_number");
//                if (phoneNumber != null && !phoneNumber.isEmpty()) {
//                    Log.d("OneClickLogin", "方法4 系统设置: " + phoneNumber);
//                    return formatPhoneNumber(phoneNumber);
//                }
//            } catch (Exception e) {
//                Log.e("OneClickLogin", "系统设置方法失败", e);
//            }
//
//        } catch (Exception e) {
//            Log.e("OneClickLogin", "获取手机号失败", e);
//        }

        return null;
    }

    private String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return null;

        // 去掉空格和特殊字符
        phoneNumber = phoneNumber.replaceAll("[\\s\\-()]", "");

        // 去掉国家码
        if (phoneNumber.startsWith("+86")) {
            phoneNumber = phoneNumber.substring(3);
        } else if (phoneNumber.startsWith("86") && phoneNumber.length() > 11) {
            phoneNumber = phoneNumber.substring(2);
        }

        // 验证是否是有效的手机号
        if (phoneNumber.matches("^1[3-9]\\d{9}$")) {
            // 添加掩码
            return phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(7);
        }

        return null;
    }

    private String getSimOperatorName() {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                String operator = telephonyManager.getSimOperator();
                Log.d("OneClickLogin", "SIM运营商代码: " + operator);

                // 根据运营商代码返回名称
                if ("46000".equals(operator) || "46002".equals(operator) || "46007".equals(operator) || "46008".equals(operator)) {
                    return "中国移动";
                } else if ("46001".equals(operator) || "46006".equals(operator) || "46009".equals(operator)) {
                    return "中国联通";
                } else if ("46003".equals(operator) || "46005".equals(operator) || "46011".equals(operator)) {
                    return "中国电信";
                } else if ("46004".equals(operator)) {
                    return "中国卫通";
                } else if ("46020".equals(operator)) {
                    return "中国铁通";
                }

                // 也可以直接获取运营商名称
                String operatorName = telephonyManager.getSimOperatorName();
                if (operatorName != null && !operatorName.isEmpty()) {
                    Log.d("OneClickLogin", "运营商名称: " + operatorName);
                    return operatorName;
                }
            }
        } catch (Exception e) {
            Log.e("OneClickLogin", "获取运营商信息失败", e);
        }
        return "";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PHONE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 取电话号码掩码，提升用户体验
                AuthHelper.getInstance().umcLoginPre(new AuthListener(1));
            }
        }
    }

    private void displayPhoneNumberOrOperator() {
        // 暂时固定显示文案
        if (tvPhoneNumber != null) {
            tvPhoneNumber.setText("灵犀");
        }
    }

    @Override
    protected void showToast(String message) {
        GlobalToast.show(this, message, GlobalToast.Type.NORMAL);
    }
    
    private void checkIfFromHomePage() {
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("from_home", false)) {
            // 从首页跳转过来，显示登录提示对话框
//            showLoginPromptDialog();
//            showToast("登录可体验完整功能");
            tabIndex = intent.getIntExtra("selected_tab", -1);
            GlobalToast.show(this, "登录可体验完整功能", GlobalToast.Type.NORMAL);
        }
    }
    private void showLoginPromptDialog() {
        LoginPromptDialog dialog = new LoginPromptDialog(this);
        dialog.show();

        // 2秒后自动关闭
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }, 2000);
    }
    
    private final class AuthListener implements TokenListener {
        private final int mFeature;

        public AuthListener(int feature) {
            mFeature = feature;
        }

        /**
         * 认证登录回调接口
         * @param jObj 回调响应参数
         *             resultCode：接口返回码，“103000”为成功
         *             resultDesc：返回码描述
         *             securityphone：电话号码掩码
         *             loginMethod：登录的方法
         *             operatortype：运营商类型：0 未知；1 移动；2 联通；3 电信
         *             usetimes：预取号使用的时间，单位毫秒
         *             traceId：SDK生成的本次会话标识id，用于排查问题，长度32位
         *             imageUrl：品牌图片的URL地址
         *             brand：品牌名称
         *             gsmLevel：品牌等级
         *             token：有效期2min，一次有效，同一用户（手机号）10分钟内获取token且未使用的数量不超过30个
         */
        @Override
        public void onGetTokenComplete(JSONObject jObj) {
            if (jObj != null) {
                switch (mFeature) {
                    case 1:
                        String securityPhone = jObj.optString("securityphone");
                        btnOneClickLogin.setText("使用" + securityPhone + "一键登录");
                        Log.d("OneClickLogin", "预取号成功");
                        break;
                    case 2:
                        String token = jObj.optString("token");
                        // 使用token调用后端接口进行登录
                        viewModel.performOneClickLogin(token);
                        break;
                    case 3:
                        break;
                }
            }
        }
    }
}