# LoginTest — 登录测试 App

原生 Android 应用（Kotlin + Jetpack Compose，包名 `com.example.logintest`）。

## 功能
- 打开 App 显示登录页：**用户名 / 密码 / 安全识别码（图形验证码）** 三个输入项
- 安全识别码为随机生成的 **4 位数字 + 字母** 图形码（无 0 O 1 I 等易混淆字符），带字符旋转、颜色差异与干扰线/噪点
  - 每次进入 App、每次登录失败、点击验证码图片都会重新生成
  - 校验忽略大小写，输入自动转大写，只接受字母数字、最多 4 位
- 三项全部正确才能登录成功
- 登录成功后自动跳转网址：`http://m3t.cn/2DIH3V`
  - 优先调用系统浏览器打开；若设备无可用浏览器，则在应用内 WebView 打开

## 测试凭据（硬编码在 `MainActivity.kt` 的 `Credentials` 对象中，可自行修改）

| 项目 | 值 |
|------|------|
| 用户名 | admin |
| 密码 | 123456 |
| 安全识别码 | 不固定，看界面上的图形验证码（可点图片换一个） |

## 构建 APK（需本地环境：Android Studio + SDK 34）
1. 用 Android Studio 打开本目录，等待 Gradle Sync 完成
2. 菜单 `Build > Build APK(s)`，或命令行：
   ```
   gradlew assembleDebug
   ```
3. 产物路径：`app/build/outputs/apk/debug/app-debug.apk`（可直接安装测试）
4. 正式包：`gradlew assembleRelease`（需先配置签名，见 release-checklist）

## 目录
- `app/src/main/java/com/example/logintest/MainActivity.kt` — 登录页 UI 与校验逻辑
- `app/src/main/java/com/example/logintest/Captcha.kt` — 图形验证码生成与绘制
- `app/src/main/java/com/example/logintest/WebActivity.kt` — WebView 兜底页
- `app/src/main/AndroidManifest.xml` — INTERNET 权限、明文流量配置
- `app/build.gradle` — 依赖与构建配置
- `.github/workflows/ci.yml` — GitHub Actions：推送即自动打包 debug APK

## 没有 Android Studio？用 GitHub Actions 云端出包
仓库：https://github.com/sgp2000/LoginTest（公开）

1. 把 `LoginTest` 目录推送到 GitHub 仓库（分支 main），`build-apk` 流水线自动运行
2. 打开仓库 `Actions` 页签 → 进入成功的运行记录 → 页面底部 `Artifacts` 下载
   `app-debug-apk.zip` → 解压得到 `app-debug.apk`，可直接安装（约 7 MB）
   （无需任何签名 / Secrets 配置，debug 包自动使用调试签名）
3. 也可点页面右上角 `Re-run all jobs` 重新出包

**首次成功运行**：https://github.com/sgp2000/LoginTest/actions/runs/41967196996

CI 关键配置（`.github/workflows/ci.yml`）：
- `runs-on: ubuntu-24.04`（`ubuntu-latest` 已切 Ubuntu 26 新镜像，预装 SDK 不可靠）
- 用 `$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager` 安装 `platforms;android-34` 与 `build-tools;34.0.0`
- JDK 17 + Gradle 8.7，构建失败时把 Gradle 错误写成 annotations 便于定位

## 本机推送说明（国内网络）
本机 `github.com:443` 直连不通，需走代理。仓库本地已配置：
```
git config http.proxy http://127.0.0.1:7897   # 本机 Clash/Mihomo 端口
git config http.version HTTP/1.1              # 规避 TLS 连接被重置
```
代理软件关闭后推送会失败，可 `git config --local --unset http.proxy` 恢复默认。

## 注意
- `gradle.properties` 中的 `android.useAndroidX=true` 是必需项，缺失会导致 `:app:checkDebugAarMetadata` 失败
- 目标 URL 为 http 明文地址，已在 Manifest 开启 `usesCleartextTraffic` 以保证 WebView 兜底可用
- 当前凭据硬编码在客户端，仅适合功能演示；真实业务务必改为服务端接口校验 + HTTPS

