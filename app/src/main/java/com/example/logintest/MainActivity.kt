package com.example.logintest

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ==================== 账号凭据配置（测试用，可自行修改） ====================
object Credentials {
    const val USERNAME = "admin"       // 用户名
    const val PASSWORD = "123456"      // 密码

    /** 登录成功后跳转的网址 */
    const val TARGET_URL = "http://m3t.cn/2DIH3V"
}
// ==========================================================================
// 安全识别码是图形验证码，不需要配置：每次进入 App、登录失败或点击图片时随机生成，
// 由 Captcha.generate() 产出 4 位「数字 + 字母」，校验时忽略大小写。

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                LoginScreen()
            }
        }
    }
}

@Composable
fun LoginScreen() {
    val context = LocalContext.current

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var securityCode by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // 当前图形验证码，进入页面即随机生成
    var captcha by remember { mutableStateOf(Captcha.generate()) }

    // 刷新验证码并清空输入（点击图片、校验失败时调用）
    fun refreshCaptcha() {
        captcha = Captcha.generate()
        securityCode = ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "账号登录",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it; errorMsg = null },
            label = { Text("用户名") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMsg = null },
            label = { Text("密码") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 安全识别码输入框 + 图形验证码
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = securityCode,
                onValueChange = { input ->
                    // 只允许字母数字、自动转大写、不超过 4 位
                    securityCode = input.filter { it.isLetterOrDigit() }
                        .take(Captcha.LENGTH)
                        .uppercase()
                    errorMsg = null
                },
                label = { Text("安全识别码") },
                placeholder = { Text("${Captcha.LENGTH} 位") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                modifier = Modifier.weight(1f)
            )

            CaptchaImage(
                code = captcha,
                onRefresh = { refreshCaptcha() }
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "看不清？点击右侧图片可刷新",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

        // 错误提示
        errorMsg?.let { msg ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = msg,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                when {
                    username.isBlank() || password.isBlank() || securityCode.isBlank() -> {
                        errorMsg = "请填写所有输入项"
                    }

                    !Captcha.matches(securityCode, captcha) -> {
                        errorMsg = "安全识别码错误，已刷新，请重新输入"
                        refreshCaptcha()
                    }

                    username.trim() != Credentials.USERNAME -> {
                        errorMsg = "用户名错误"
                        refreshCaptcha()
                    }

                    password != Credentials.PASSWORD -> {
                        errorMsg = "密码错误"
                        refreshCaptcha()
                    }

                    else -> {
                        // 全部正确，登录成功 → 跳转目标网址
                        errorMsg = null
                        openTargetUrl(context)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("登 录", fontSize = 18.sp)
        }
    }
}

/** 优先用系统浏览器打开目标网址；若无可用浏览器则在应用内 WebView 打开 */
private fun openTargetUrl(context: android.content.Context) {
    val uri = Uri.parse(Credentials.TARGET_URL)
    val browserIntent = Intent(Intent.ACTION_VIEW, uri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(browserIntent)
    } catch (e: ActivityNotFoundException) {
        // 兜底：应用内 WebView
        val webIntent = Intent(context, WebActivity::class.java).apply {
            putExtra(WebActivity.EXTRA_URL, Credentials.TARGET_URL)
        }
        context.startActivity(webIntent)
    }
}
