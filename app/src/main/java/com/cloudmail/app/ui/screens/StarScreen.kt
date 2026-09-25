package com.cloudmail.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cloudmail.app.data.remote.dto.EmailDto
import com.cloudmail.app.ui.components.EmailListItem
import com.cloudmail.app.ui.components.EmptyState
import com.cloudmail.app.ui.components.LoadingBox
import com.cloudmail.app.ui.viewmodel.StarViewModel
import kotlinx.coroutines.flow.snapshotFlow

/**
 * 星标列表：手动刷新 + 触底加载 + 取消星标。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StarScreen(
    onOpenEmail: (EmailDto) -> Unit,
    viewModel: StarViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

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

    PullToRefreshBox(
        isRefreshing = state.loading,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(Modifier.fillMaxSize()) {
            when {
                state.emails.isEmpty() && !state.loading -> EmptyState(
                    title = "暂无星标邮件",
                    subtitle = "在邮件详情里点亮 ★ 即可收藏",
                    modifier = Modifier.weight(1f)
                )
                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(state.emails, key = { it.emailId }) { email ->
                        EmailListItem(
                            email = email,
                            onClick = { onOpenEmail(email) }
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
}
