package com.cloudmail.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cloudmail.app.data.remote.dto.AccountDto
import com.cloudmail.app.ui.components.AvatarInitial
import com.cloudmail.app.ui.components.EmptyState
import com.cloudmail.app.ui.theme.BrandGradient
import com.cloudmail.app.ui.viewmodel.AccountViewModel
import com.cloudmail.app.util.HtmlUtils

/**
 * 账号管理：绑定新邮箱、重命名、置顶、全部接收、删除。
 */
@Composable
fun AccountScreen(
    onBack: () -> Unit,
    viewModel: AccountViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showRenameDialog by remember { mutableStateOf<AccountDto?>(null) }
    var renameValue by remember { mutableStateOf("") }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
                Text("邮箱账号", style = MaterialTheme.typography.titleLarge)
            }

            when {
                state.accounts.isEmpty() && !state.loading -> EmptyState(
                    title = "还没有账号",
                    subtitle = "添加你的第一个邮箱账号",
                    modifier = Modifier.weight(1f)
                )
                else -> LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(state.accounts, key = { it.accountId }) { account ->
                        AccountItem(
                            account = account,
                            onRename = {
                                renameValue = account.name ?: ""
                                showRenameDialog = account
                            },
                            onSetAllReceive = { viewModel.setAllReceive(account.accountId) },
                            onSetTop = { viewModel.setAsTop(account.accountId) },
                            onDelete = { viewModel.delete(account.accountId) }
                        )
                    }
                    item { Box(Modifier.padding(vertical = 64.dp)) }
                }
            }

            if (state.error != null) {
                Text(
                    state.error.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }
        }

        // 添加账号 FAB
        IconButton(
            onClick = { viewModel.showAddDialog(true) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .background(BrandGradient, RoundedCornerShape(28.dp))
                .padding(10.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "添加账号", tint = Color.White)
        }

        // 添加账号对话框
        AddAccountDialog(
            visible = state.showAddDialog,
            onDismiss = { viewModel.showAddDialog(false) },
            onAdd = { email -> viewModel.add(email) }
        )

        // 重命名对话框
        showRenameDialog?.let { account ->
            AlertDialog(
                onDismissRequest = { showRenameDialog = null },
                title = { Text("重命名账号") },
                text = {
                    OutlinedTextField(
                        value = renameValue,
                        onValueChange = { renameValue = it },
                        label = { Text("显示名称") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.rename(account.accountId, renameValue)
                        showRenameDialog = null
                    }) { Text("确定") }
                },
                dismissButton = {
                    TextButton(onClick = { showRenameDialog = null }) { Text("取消") }
                }
            )
        }
    }
}

@Composable
private fun AccountItem(
    account: AccountDto,
    onRename: () -> Unit,
    onSetAllReceive: () -> Unit,
    onSetTop: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarInitial(email = account.email, name = account.name, size = 40)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = account.name ?: HtmlUtils.emailName(account.email ?: ""),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                if (account.allReceive == 1) {
                    Text(
                        " 全部接收",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Text(
                text = account.email ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onRename) {
            Icon(
                Icons.Filled.DriveFileRenameOutline,
                contentDescription = "重命名",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onSetTop) {
            Icon(
                Icons.Filled.KeyboardArrowUp,
                contentDescription = "置顶",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onSetAllReceive) {
            Icon(
                Icons.Filled.NotificationsActive,
                contentDescription = "全部接收",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Filled.DeleteOutline,
                contentDescription = "删除",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun AddAccountDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    if (!visible) return
    var email by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加邮箱账号") },
        text = {
            Column {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("邮箱地址") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "支持本站域下的任意邮箱；服务端未开启人机验证时无需验证码。",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onAdd(email) }) { Text("添加") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
