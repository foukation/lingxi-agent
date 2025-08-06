package com.fxzs.lingxiagent.view.user;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.fxzs.lingxiagent.MainActivity;
import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.view.common.BaseActivity;
import com.fxzs.lingxiagent.view.common.DataBindingUtils;
import com.fxzs.lingxiagent.view.common.EditInfoDialog;
import com.fxzs.lingxiagent.viewmodel.user.VMAccountInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AccountInfoActivity extends BaseActivity<VMAccountInfo> {
    
    private static final String TAG = "AccountInfoActivity";
    
    private ImageView ivBack;
    private TextView tvTitle;
    private ImageView ivAvatar;
    private TextView tvChangeAvatar;
    private LinearLayout layoutNickname;
    private TextView tvNickname;
    private TextView tvPhone;
    private TextView btnLogout;
    
    private static final int REQUEST_EDIT_NAME = 1001;
    private static final int REQUEST_CAMERA = 1002;
    private static final int REQUEST_GALLERY = 1003;
    private static final int REQUEST_FILE = 1004;
    private static final int REQUEST_CAMERA_PERMISSION = 1005;
    private static final int REQUEST_STORAGE_PERMISSION = 1006;
    
    private String currentPhotoPath;
    private Uri photoUri;
    
    @Override
    protected int getLayoutResource() {
        return R.layout.activity_account_info;
    }
    
    @Override
    protected Class<VMAccountInfo> getViewModelClass() {
        return VMAccountInfo.class;
    }
    
    @Override
    protected void initializeViews() {
        // 初始化控件
        ivBack = findViewById(R.id.iv_back);
        tvTitle = findViewById(R.id.tv_title);
        ivAvatar = findViewById(R.id.iv_avatar);
        tvChangeAvatar = findViewById(R.id.tv_change_avatar);
        layoutNickname = findViewById(R.id.layout_nickname);
        tvNickname = findViewById(R.id.tv_nickname);
        tvPhone = findViewById(R.id.tv_phone);
        btnLogout = findViewById(R.id.btn_logout);

        // 设置状态栏颜色为白色，与背景一致，并保证内容不被遮挡
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(android.graphics.Color.parseColor("#FFFFFF"));
            getWindow().getDecorView().postDelayed(() -> {
                getWindow().setStatusBarColor(android.graphics.Color.parseColor("#FFFFFF"));
            }, 100);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                getWindow().getDecorView().setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
        // 根布局加上android:fitsSystemWindows="true"（如未加）
        
        // 设置点击事件
        ivBack.setOnClickListener(v -> finish());
        
        ivAvatar.setOnClickListener(v -> showAvatarPopup());
        tvChangeAvatar.setOnClickListener(v -> showAvatarPopup());
        
        layoutNickname.setOnClickListener(v -> {
            showEditNameDialog();
        });
        
        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }
    
    @Override
    protected void setupDataBinding() {
        // 绑定昵称
        DataBindingUtils.bindTextView(tvNickname, viewModel.getNickname(), this);
        
        // 绑定手机号
        DataBindingUtils.bindTextView(tvPhone, viewModel.getPhone(), this);
    }
    
    @Override
    protected void setupObservers() {
        // 观察头像变化
        viewModel.getAvatarUrl().observe(this, avatarUrl -> {
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Object loadUrl = avatarUrl;
                if (avatarUrl.startsWith("file://")) {
                    loadUrl = new File(avatarUrl.substring(7));
                }
                int radiusPx = (int) (12.8f * getResources().getDisplayMetrics().density + 0.5f);
                Glide.with(this)
                    .load(loadUrl)
                    .placeholder(R.drawable.icon_user_head)
                    .error(R.drawable.icon_user_head)
                    .transform(new CenterCrop(), new RoundedCorners(radiusPx))
                    .into(ivAvatar);
            }
        });
        
        // 观察头像上传结果
//        viewModel.getAvatarUploadResult().observe(this, success -> {
//            if (success != null && success) {
//                showToast("头像更新成功");
//            }
//        });
        
        // 观察退出登录结果
        viewModel.getLogoutResult().observe(this, success -> {
            if (success != null && success) {
                // 退出成功，跳转到首页对话
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("selected_tab", 0); // 选中对话Tab
                startActivity(intent);
                finish();
            }
        });
    }
    
    private void showAvatarPopup() {
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_avatar_select, null);
        final PopupWindow popupWindow = new PopupWindow(popupView, dp2px(200), LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setElevation(dp2px(12));

        popupView.findViewById(R.id.item_camera).setOnClickListener(v -> {
            popupWindow.dismiss();
            checkCameraPermission();
        });
        popupView.findViewById(R.id.item_gallery).setOnClickListener(v -> {
            popupWindow.dismiss();
            checkStoragePermission();
        });
        popupView.findViewById(R.id.item_file).setOnClickListener(v -> {
            popupWindow.dismiss();
            openFileManager();
        });

        // 锚定在头像下方，向下偏移8dp
        popupWindow.showAsDropDown(ivAvatar, 0, dp2px(8));
    }

    private int dp2px(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }
    
    private void checkCameraPermission() {
        Log.d(TAG, "Checking camera permission");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission not granted, requesting...");
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.CAMERA}, 
                REQUEST_CAMERA_PERMISSION);
        } else {
            Log.d(TAG, "Camera permission already granted");
            openCamera();
        }
    }
    
    private void checkStoragePermission() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? 
            Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;
            
        if (ContextCompat.checkSelfPermission(this, permission) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                new String[]{permission}, 
                REQUEST_STORAGE_PERMISSION);
        } else {
            openGallery();
        }
    }
    
    private void openCamera() {
        Log.d(TAG, "Opening camera");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
                Log.d(TAG, "Created image file: " + photoFile.getAbsolutePath());
            } catch (IOException ex) {
                Log.e(TAG, "Error creating image file", ex);
                showToast("创建图片文件失败");
                ex.printStackTrace();
                return;
            }
            
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider",
                    photoFile);
                Log.d(TAG, "Photo URI: " + photoUri);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_CAMERA);
            }
        } else {
            Log.e(TAG, "No camera app found");
            showToast("没有找到相机应用");
        }
    }
    
    private void openGallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, REQUEST_GALLERY);
    }

    private void openFileManager() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "选择图片文件"), REQUEST_FILE);
        } catch (android.content.ActivityNotFoundException ex) {
            showToast("请安装文件管理器");
        }
    }
    
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir("Pictures");
        
        // 确保目录存在
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        
        File image = File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                showToast("需要相机权限才能拍照");
            }
        } else if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                showToast("需要存储权限才能选择图片");
            }
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);
        
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                // 拍照完成
                Log.d(TAG, "Camera result OK, currentPhotoPath: " + currentPhotoPath);
                if (currentPhotoPath != null) {
                    viewModel.uploadAvatar(currentPhotoPath);
                } else {
                    showToast("拍照失败，请重试");
                }
            } else if (requestCode == REQUEST_GALLERY) {
                // 从相册选择完成
                if (data != null && data.getData() != null) {
                    Uri selectedImage = data.getData();
                    Log.d(TAG, "Gallery result OK, uri: " + selectedImage);
                    String imagePath = copyUriToFile(selectedImage);
                    if (imagePath != null) {
                        Log.d(TAG, "Copied image to: " + imagePath);
                        viewModel.uploadAvatar(imagePath);
                    } else {
                        showToast("获取图片路径失败");
                    }
                }
            } else if (requestCode == REQUEST_FILE) {
                // 从文件管理器选择完成
                if (data != null && data.getData() != null) {
                    Uri selectedFile = data.getData();
                    Log.d(TAG, "File result OK, uri: " + selectedFile);
                    String filePath = copyUriToFile(selectedFile);
                    if (filePath != null) {
                        Log.d(TAG, "Copied file to: " + filePath);
                        viewModel.uploadAvatar(filePath);
                    } else {
                        showToast("获取文件路径失败");
                    }
                }
            }
        } else {
            Log.d(TAG, "Result not OK: " + resultCode);
        }
    }
    
    private String copyUriToFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            
            File outputFile = createImageFile();
            OutputStream outputStream = new FileOutputStream(outputFile);
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            inputStream.close();
            outputStream.close();
            
            return outputFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private void showEditNameDialog() {
        EditInfoDialog editDialog = new EditInfoDialog(this)
            .setTitle("修改名字")
            .setHint("请输入名字")
            .setText(viewModel.getNickname().get())
            .setCancelText("取消")
            .setConfirmText("确定")
            .setMaxLength(20)
            .setOnEditInfoDialogListener(new EditInfoDialog.OnEditInfoDialogListener() {
                @Override
                public void onConfirm(String inputText) {
                    if (inputText.isEmpty()) {
                        showToast("昵称不能为空");
                        return;
                    }
                    // 更新昵称
                    viewModel.updateNickname(inputText);
                }
                
                @Override
                public void onCancel() {
                    // 取消编辑
                }
            });
        editDialog.show();
    }
    
    private void showLogoutDialog() {
        LogoutDialog.show(this, new LogoutDialog.OnLogoutListener() {
            @Override
            public void onConfirm() {
                viewModel.logout();
            }

            @Override
            public void onCancel() {
                // 取消退出
            }
        });
    }
}
