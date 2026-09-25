package com.cloudmail.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cloudmail.app.data.remote.dto.AccountDto
import com.cloudmail.app.data.remote.dto.EmailDto
import com.cloudmail.app.data.remote.dto.EmailType
import com.cloudmail.app.ui.components.EmailListItem
import com.cloudmail.app.ui.components.EmptyState
import com.cloudmail.app.ui.components.GradientFab
import com.cloudmail.app.ui.components.LoadingBox
import com.cloudmail.app.ui.theme.BrandGradient
import com.cloudmail.app.ui.viewmodel.InboxViewModel
import com.cloudmail.app.util.HtmlUtils
import kotlinx.coroutines.flow.snapshotFlow

/**
 * 收件箱：账号切换 + 收件/已发 + 下拉刷新 + 触底加载 + 写信 FAB。
 * 实时性：用户手动刷新（无长轮询/推送）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    onOpenEmail: (EmailDto) -> Unit,
    onOpenCompose: () -> Unit,
    onOpenAccounts: () -> Unit,
    viewModel: InboxViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // 触底加载更早邮件
    LaunchedEffect(listState) {
        snapshotFlow {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = info.totalItemsCount
            total > 0 && last >= total - 4
        }.collect { nearEnd ->
            if (nearEnd) viewModel.loadMore()
        }
    }

    Box(Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = state.refreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize()
        ) {
            Column(Modifier.fillMaxSize()) {
                // 账号选择条（水平）
                AccountSelector(
                    accounts = state.accounts,
                    selectedId = state.selectedAccount?.accountId ?: 0L,
                    onSelect = { viewModel.selectAccount(it) },
                    onOpenAccounts = onOpenAccounts
                )

                // 收件 / 已发
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    SegmentedButton(
                        selected = state.boxType == EmailType.RECEIVE,
                        onClick = { viewModel.setBoxType(EmailType.RECEIVE) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) { Text("收件箱") }
                    SegmentedButton(
                        selected = state.boxType == EmailType.SEND,
                        onClick = { viewModel.setBoxType(EmailType.SEND) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) { Text("已发送") }
                }

                when {
                    state.initialLoading -> LoadingBox(Modifier.weight(1f))
                    state.selectedAccount == null -> EmptyState(
                        title = "还没有邮箱账号",
                        subtitle = "请先添加一个邮箱账号",
                        modifier = Modifier.weight(1f)
                    )
                    state.emails.isEmpty() && !state.refreshing -> EmptyState(
                        title = if (state.boxType == EmailType.RECEIVE) "收件箱空空如也" else "还没有发送过邮件",
                        subtitle = "下拉可刷新",
                        modifier = Modifier.weight(1f)
                    )
                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(bottom = 96.dp)
                        ) {
                            items(
                                items = state.emails,
                                key = { it.emailId }
                            ) { entity ->
                                val dto = remember(entity.emailId) {
                                    EmailDto(
                                        emailId = entity.emailId,
                                        sendEmail = entity.sendEmail,
                                        name = entity.name,
                                        accountId = entity.accountId,
                                        userId = entity.userId,
                                        subject = entity.subject,
                                        text = entity.text,
                                        content = entity.content,
                                        toEmail = entity.toEmail,
                                        toName = entity.toName,
                                        recipient = entity.recipient,
                                        type = entity.type,
                                        status = entity.status,
                                        unread = entity.unread,
                                        isStar = entity.isStar,
                                        createTime = entity.createTime
                                    )
                                }
                                EmailListItem(
                                    email = dto,
                                    onClick = {
                                        if (entity.unread == 1) viewModel.markRead(entity.emailId)
                                        onOpenEmail(dto)
                                    }
                                )
                            }
                            if (state.loadingMore) {
                                item { LoadingBox(Modifier.fillMaxWidth().padding(vertical = 16.dp)) }
                            }
                            if (state.noMore && state.emails.isNotEmpty()) {
                                item {
                                    Text(
                                        "没有更多了",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 20.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                if (state.error != null) {
                    Text(
                        state.error.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // 写信 FAB
        GradientFab(
            onClick = onOpenCompose,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        )
    }
}

/** 账号选择条：水平滚动 chips，末位为"管理"入口 */
@Composable
private fun AccountSelector(
    accounts: List<AccountDto>,
    selectedId: Long,
    onSelect: (AccountDto) -> Unit,
    onOpenAccounts: () -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(accounts, key = { it.accountId }) { account ->
            val selected = account.accountId == selectedId
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .background(
                        brush = if (selected) BrandGradient
                        else androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onSelect(account) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = account.name ?: HtmlUtils.emailName(account.email ?: ""),
                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
        item {
            Box(
                modifier = Modifier
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onOpenAccounts() }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    "管理",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
