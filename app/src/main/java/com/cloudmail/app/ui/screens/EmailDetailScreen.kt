package com.cloudmail.app.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Forward
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cloudmail.app.data.remote.dto.EmailDto
import com.cloudmail.app.data.remote.dto.EmailType
import com.cloudmail.app.ui.components.GradientButton
import com.cloudmail.app.ui.viewmodel.EmailDetailViewModel
import com.cloudmail.app.util.HtmlUtils

/**
 * 邮件详情：WebView 渲染 HTML 正文（禁 JS），顶部操作：回复/转发/删除/星标。
 * 附件区降级展示（服务端未开通 R2）。
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun EmailDetailScreen(
    emailJson: String,
    onBack: () -> Unit,
    onReply: (EmailDto) -> Unit,
    onForward: (EmailDto) -> Unit,
    onDeleted: () -> Unit,
    viewModel: EmailDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(emailJson) { viewModel.init(emailJson) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val email = state.email

    Column(Modifier.fillMaxSize()) {
        // 顶部毛玻璃
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
                text = email?.subject ?: "邮件详情",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { viewModel.toggleStar() }) {
                Icon(
                    imageVector = if (email?.isStar == 1) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = "星标",
                    tint = if (email?.isStar == 1) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = {
                email?.let { onForward(it) }
            }) {
                Icon(Icons.Filled.Forward, contentDescription = "转发")
            }
            IconButton(onClick = {
                email?.let { viewModel.delete(onDeleted) }
            }) {
                Icon(Icons.Filled.DeleteOutline, contentDescription = "删除")
            }
        }

        if (email == null) {
            com.cloudmail.app.ui.components.LoadingBox(Modifier.weight(1f))
            return
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // 发件人信息
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                com.cloudmail.app.ui.components.AvatarInitial(
                    email = email.sendEmail,
                    name = HtmlUtils.displayName(
                        if (email.type == EmailType.SEND) email.toEmail else email.sendEmail
                    ),
                    size = 44
                )
                Column(Modifier.padding(start = 12.dp).weight(1f)) {
                    Text(
                        text = HtmlUtils.displayName(
                            if (email.type == EmailType.SEND) email.toEmail else email.sendEmail
                        ),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = if (email.type == EmailType.SEND) email.toEmail.orEmpty()
                        else email.sendEmail.orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = com.cloudmail.app.util.DateUtils.format(email.createTime),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                if (email.isStar == 1) {
                    Text("★", color = MaterialTheme.colorScheme.tertiary)
                }
            }

            // 正文
            val html = email.content?.takeIf { it.isNotBlank() } ?: HtmlUtils.textToHtml(email.text)
            var webHeight by remember { androidx.compose.runtime.mutableStateOf(0) }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = false
                            settings.domStorageEnabled = false
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true
                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    view?.evaluateJavascript(
                                        "(function(){return document.documentElement.scrollHeight})()"
                                    ) { value ->
                                        val px = value.trim().removeSuffix(".0").toIntOrNull() ?: 0
                                        webHeight = px
                                    }
                                }
                            }
                            loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            with(androidx.compose.ui.platform.LocalDensity.current) {
                                if (webHeight > 0) webHeight.toDp() else 320.dp
                            }
                        )
                        .padding(vertical = 8.dp)
                )
            }

            // 附件区：产品裁剪，服务端未开通 R2，降级提示
            val attCount = email.attList?.size ?: 0
            if (attCount > 0) {
                Text(
                    "此邮件包含 $attCount 个附件，当前服务端未启用附件存储，无法预览/下载",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // 回复按钮
            GradientButton(
                text = if (email.type == EmailType.RECEIVE) "回复" else "回复全部",
                onClick = { onReply(email) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}
