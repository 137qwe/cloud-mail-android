package com.cloudmail.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cloudmail.app.ui.components.AvatarInitial
import com.cloudmail.app.ui.components.GradientButton
import com.cloudmail.app.ui.viewmodel.ProfileViewModel

/**
 * 我的：用户信息、修改密码、管理入口提示、退出登录。
 */
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var newPwd by remember { mutableStateOf("") }
    var confirmPwd by remember { mutableStateOf("") }
    var pwdError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        val user = state.userInfo
        // 用户信息卡
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarInitial(email = user?.email, name = user?.name, size = 56)
            Column(Modifier.padding(start = 14.dp)) {
                Text(
                    text = user?.name ?: "未登录",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = user?.email ?: "",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = buildString {
                        append("角色：")
                        append(user?.role?.name ?: (if (viewModel.isAdmin()) "管理员" else "普通用户"))
                        append(" · 今日发送余量：")
                        append(user?.sendCount ?: "-")
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // 管理入口（仅管理员显示提示）
        if (viewModel.isAdmin()) {
            Text(
                "管理员：用户/角色/注册码/系统设置等管理功能请在浏览器访问 Web 管理端。",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }

        if (state.error != null) {
            Text(
                state.error.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        GradientButton(
            text = "修改密码",
            onClick = { viewModel.showChangePwdDialog(true) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )
        GradientButton(
            text = "退出登录",
            onClick = { onLogout() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        )
    }

    // 修改密码对话框
    if (state.showChangePwdDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showChangePwdDialog(false) },
            title = { Text("修改密码") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newPwd,
                        onValueChange = { newPwd = it },
                        label = { Text("新密码（至少 6 位）") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPwd,
                        onValueChange = { confirmPwd = it },
                        label = { Text("确认新密码") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                    if (pwdError != null) {
                        Text(
                            pwdError.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newPwd.length < 6) {
                        pwdError = "密码至少 6 位"
                    } else if (newPwd != confirmPwd) {
                        pwdError = "两次输入不一致"
                    } else {
                        pwdError = null
                        viewModel.changePassword(newPwd) {
                            newPwd = ""
                            confirmPwd = ""
                        }
                    }
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showChangePwdDialog(false) }) { Text("取消") }
            }
        )
    }
}
