# API配置说明

## API文档
完整的API文档请查看：https://apifox.com/apidoc/shared/c3156753-074b-4515-b2c6-bd36577e0084

## 配置后台API地址

请在 `Constants.java` 文件中修改 `BASE_URL` 为你的实际后台地址：

```java
// 文件路径：app/src/main/java/com/example/iyaproject/model/common/Constants.java

public static final String BASE_URL = "https://api.example.com/";
```

将 `https://api.example.com/` 替换为你的实际后台API地址。

## API接口列表

已实现的API接口包括：

### 认证相关
- **手机号+密码登录**: `POST /app-api/member/auth/login`
- **手机号+验证码登录**: `POST /app-api/member/auth/sms-login`
- **发送验证码**: `POST /app-api/member/auth/send-sms-code`
- **用户注册**: `POST /app-api/member/auth/register`
- **刷新Token**: `POST /app-api/member/auth/refresh-token`
- **退出登录**: `POST /app-api/member/auth/logout`
- **获取用户信息**: `GET /app-api/member/user/get`
- **重置密码**: `PUT /app-api/member/user/reset-password`

## 测试说明

1. 确保后台服务已启动
2. 修改 `BASE_URL` 为正确的地址
3. 如果是本地测试，确保手机/模拟器与后台在同一网络
4. 如果使用HTTP（非HTTPS），已在AndroidManifest.xml中配置了 `android:usesCleartextTraffic="true"`

## 错误处理

所有网络请求都包含了错误处理：
- 网络错误会显示Toast提示
- Token过期会自动尝试刷新（需后台支持）
- 请求失败会在ViewModel中通过 `setError()` 方法显示错误信息