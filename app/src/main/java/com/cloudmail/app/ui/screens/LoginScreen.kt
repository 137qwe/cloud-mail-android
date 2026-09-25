package com.cloudmail.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cloudmail.app.ui.components.GradientButton
import com.cloudmail.app.ui.theme.BrandGradient
import com.cloudmail.app.ui.theme.TextPrimaryLight
import com.cloudmail.app.ui.viewmodel.AuthViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle


/**
 * 登录 / 注册页：品牌渐变背景 + 圆润卡片。
 * 注册含可选注册码（服务端按配置开启 regKey）。
 */
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    onLoggedIn: () -> Unit
) {
    var isRegister by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var regCode by remember { mutableStateOf("") }

    val state by authViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.registered) {
        if (state.registered && !state.needManualLogin) {
            onLoggedIn()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandGradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text("☁", fontSize = 36.sp, color = androidx.compose.ui.graphics.Color.White)
            }
            Text(
                "Cloud Mail",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.padding(top = 12.dp)
            )
            Text(
                "基于 Cloudflare 的轻量邮箱客户端",
                fontSize = 14.sp,
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f),
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
            )

            // 表单卡片
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.94f))
                    .padding(20.dp)
            ) {
                Text(
                    if (isRegister) "注册新账号" else "欢迎回来",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimaryLight
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("邮箱") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("密码") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                )
                if (isRegister) {
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("确认密码") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    )
                    OutlinedTextField(
                        value = regCode,
                        onValueChange = { regCode = it },
                        label = { Text("注册码（可选）") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    )
                }

                if (state.error != null) {
                    Text(
                        state.error.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }

                GradientButton(
                    text = if (isRegister) "注册" else "登录",
                    loading = state.loading,
                    onClick = {
                        if (isRegister) {
                            if (password != confirmPassword) {
                                return@GradientButton
                            }
                            authViewModel.register(email, password, regCode)
                        } else {
                            authViewModel.login(email, password)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(onClick = { isRegister = !isRegister }) {
                        Text(
                            if (isRegister) "已有账号？去登录" else "没有账号？去注册",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
