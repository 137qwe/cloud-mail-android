# Cloud Mail Android

基于 [Cloud Mail](https://github.com/maillab/cloud-mail) 开放 API 的 Android 邮件客户端（开源）。

- **技术栈**：Kotlin + Jetpack Compose（Material 3 · 品牌渐变风）· MVVM + Repository · Hilt · Retrofit/OkHttp · Room · DataStore + Keystore 加密存储
- **实时性**：用户手动刷新（下拉刷新 / 触底分页加载），无后台推送、无长轮询
- **功能**：登录 / 注册 / 登出、多邮箱账号绑定与管理、收件箱 / 已发送、邮件详情（WebView 渲染 HTML）、回复 / 转发、星标、修改密码、按角色展示管理入口
- **裁剪项**：附件上传 / 下载（服务端未开通 R2 对象存储时不可用）、人机验证（服务端未开启 Turnstile）、后台推送通知

## 快速开始

### 环境要求

- JDK 17
- Android SDK（compileSdk 34）

### 构建

```bash
# 指向你自己的 Cloud Mail 部署（末尾带 /）
./gradlew assembleRelease -PapiBaseUrl="https://mail.example.com/"

# Debug 包
./gradlew assembleDebug -PapiBaseUrl="https://mail.example.com/"
```

APK 输出：`app/build/outputs/apk/release/` 或 `app/build/outputs/apk/debug/`。

> `apiBaseUrl` 默认值为 `https://mail.example.com/`，未覆盖时仅用于占位，请务必通过 `-PapiBaseUrl` 指定真实地址。

## GitHub Actions 自动编译

仓库已内置 `.github/workflows/build-apk.yml`：push / tag `v*` / 手动触发时自动编译 release APK 并上传产物；打 tag 时自动附加到 GitHub Release。

### 签名配置（可选）

在仓库 **Settings → Secrets and variables → Actions** 中配置：

| Secrets / Variables | 说明 |
|---|---|
| `ANDROID_KEYSTORE_BASE64`（Secret） | keystore 文件 base64（`base64 -w0 keystore.jks`） |
| `KEYSTORE_PASSWORD`（Secret） | keystore 密码 |
| `KEY_ALIAS`（Secret） | 别名 |
| `KEY_PASSWORD`（Secret） | 别名密码 |
| `API_BASE_URL`（Variable） | 你的 Cloud Mail 部署地址，如 `https://mail.example.com/` |

未配置签名 secrets 时，release 会回退使用 debug 签名（可安装，不建议分发）。

本地签名：在工程根目录创建 `keystore.properties`（已加入 `.gitignore`，**勿提交**）：

```properties
storeFile=keystore.jks
storePassword=xxx
keyAlias=xxx
keyPassword=xxx
```

## 服务端配合项

1. **人机验证**：注册 / 添加账号接口按站点配置需要 Turnstile token。若未开启（`registerVerify` / `addEmailVerify` = CLOSE），App 无需处理验证码。
2. **发信**：需服务端 `send` 开关为 OPEN，且角色有发送权限（`/email/send` 否则返回 403，App 会提示）。
3. **附件**：收发附件依赖 R2。未绑定 R2 时，收到的邮件附件与正文内嵌图片会缺失，App 在详情页对附件区做降级提示。

## 目录结构

```
app/src/main/java/com/cloudmail/app/
├── data/          # 数据层：remote(DTO/ApiService) · local(Room/TokenStore/UserPrefs) · repository
├── di/            # Hilt 依赖注入
├── network/       # OkHttp 拦截器（JWT / 401）、统一解包
├── ui/
│   ├── components/  # 渐变按钮/毛玻璃顶栏/渐变头像/列表项/空态
│   ├── navigation/  # 根导航 + 主界面脚手架
│   ├── screens/     # 登录/收件箱/详情/写信/账号/星标/我的
│   ├── theme/       # 品牌渐变风 Color/Shape/Type/Theme
│   └── viewmodel/   # MVVM 状态层
└── util/          # 日期格式化 / HTML 工具 / Gson
```

## 许可证

[MIT](LICENSE)
