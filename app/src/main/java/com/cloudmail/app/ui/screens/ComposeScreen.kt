package com.cloudmail.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cloudmail.app.ui.components.GradientButton
import com.cloudmail.app.ui.viewmodel.ComposePrefill
import com.cloudmail.app.ui.viewmodel.ComposeViewModel
import com.cloudmail.app.util.HtmlUtils

/**
 * 写信页：收件人（逗号分隔多地址）/ 主题 / 正文（纯文本，发送时转 HTML）。
 * 按产品裁剪：无附件上传。
 */
@Composable
fun ComposeScreen(
    prefillJson: String?,
    onBack: () -> Unit,
    onSent: () -> Unit,
    viewModel: ComposeViewModel = hiltViewModel()
) {
    val prefill = remember(prefillJson) {
        prefillJson?.takeIf { it.isNotBlank() }?.let {
            runCatching { com.cloudmail.app.util.GsonHolder.gson.fromJson(it, ComposePrefill::class.java) }.getOrNull()
        }
    }
    LaunchedEffect(Unit) { viewModel.loadAccounts(prefill) }

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var recipients by remember { mutableStateOf(prefill?.recipients?.joinToString(", ") ?: "") }
    var subject by remember { mutableStateOf(prefill?.subject ?: "") }
    var body by remember { mutableStateOf(prefill?.content?.takeIf { !it.startsWith("<html") } ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

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
            Text(
                text = when (prefill?.sendType) {
                    ComposePrefill.TYPE_REPLY -> "回复邮件"
                    ComposePrefill.TYPE_FORWARD -> "转发邮件"
                    else -> "写邮件"
                },
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            // 发送账号选择
            Box {
                var menuOpen by remember { mutableStateOf(false) }
                val selected = state.accounts.firstOrNull { it.accountId == state.selectedAccountId }
                Text(
                    text = selected?.name ?: "选择账号",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .clickable { menuOpen = true }
                )
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    state.accounts.forEach { account ->
                        DropdownMenuItem(
                            text = { Text(account.name ?: HtmlUtils.emailName(account.email ?: "")) },
                            onClick = {
                                viewModel.selectAccount(account.accountId)
                                menuOpen = false
                            }
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = recipients,
                onValueChange = { recipients = it },
                label = { Text("收件人（多个用逗号分隔）") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("主题") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            )
            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text("正文") },
                minLines = 10,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            )

            if (error != null) {
                Text(
                    error.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            if (state.error != null) {
                Text(
                    state.error.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        GradientButton(
            text = "发送",
            loading = state.sending,
            onClick = {
                val list = recipients.split(",").map { it.trim() }.filter { it.isNotBlank() }
                if (list.isEmpty()) {
                    error = "请填写收件人"
                    return@GradientButton
                }
                if (subject.isBlank()) {
                    error = "请填写主题"
                    return@GradientButton
                }
                if (body.isBlank()) {
                    error = "请填写正文"
                    return@GradientButton
                }
                error = null
                viewModel.send(list, subject, body, onSent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}
