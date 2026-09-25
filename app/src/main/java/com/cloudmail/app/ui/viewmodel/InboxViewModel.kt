package com.cloudmail.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudmail.app.data.local.db.EmailDao
import com.cloudmail.app.data.local.db.EmailEntity
import com.cloudmail.app.data.repository.AccountRepository
import com.cloudmail.app.data.repository.MailRepository
import com.cloudmail.app.data.repository.UserRepository
import com.cloudmail.app.data.remote.dto.AccountDto
import com.cloudmail.app.data.remote.dto.EmailType
import com.cloudmail.app.data.remote.dto.UserInfoDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InboxViewModel @Inject constructor(
    private val mailRepository: MailRepository,
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository,
    private val emailDao: EmailDao
) : ViewModel() {

    data class UiState(
        val accounts: List<AccountDto> = emptyList(),
        val selectedAccount: AccountDto? = null,
        val userInfo: UserInfoDto? = null,
        val boxType: Int = EmailType.RECEIVE,
        val emails: List<EmailEntity> = emptyList(),
        val refreshing: Boolean = false,
        val loadingMore: Boolean = false,
        val initialLoading: Boolean = true,
        val error: String? = null,
        val noMore: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /** 当前查询维度（accountId, boxType），驱动 Room 缓存流 */
    private val query = MutableStateFlow<Pair<Long, Int>?>(null)

    private var refreshJob: Job? = null
    private var loadMoreJob: Job? = null

    init {
        viewModelScope.launch {
            query.flatMapLatest { (accountId, type) ->
                if (accountId == 0L) flowOf(emptyList())
                else emailDao.observeRecent(accountId, type, CACHE_LIMIT)
            }.collect { cached ->
                _uiState.value = _uiState.value.copy(emails = cached)
            }
        }
        loadAccounts()
    }

    fun loadAccounts() {
        viewModelScope.launch {
            try {
                val user = userRepository.loginUserInfo()
                val accounts = accountRepository.list()
                val first = accounts.firstOrNull()
                _uiState.value = _uiState.value.copy(
                    accounts = accounts,
                    userInfo = user,
                    selectedAccount = first,
                    initialLoading = false
                )
                if (first != null) {
                    query.value = first.accountId to _uiState.value.boxType
                    refresh()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    initialLoading = false,
                    error = friendlyMessage(e)
                )
            }
        }
    }

    fun selectAccount(account: AccountDto) {
        if (_uiState.value.selectedAccount?.accountId == account.accountId) return
        _uiState.value = _uiState.value.copy(selectedAccount = account, noMore = false, emails = emptyList())
        query.value = account.accountId to _uiState.value.boxType
        refresh()
    }

    fun setBoxType(type: Int) {
        if (_uiState.value.boxType == type) return
        _uiState.value = _uiState.value.copy(boxType = type, noMore = false, emails = emptyList())
        _uiState.value.selectedAccount?.let { query.value = it.accountId to type }
        refresh()
    }

    /** 下拉刷新：拉最新一页并替换本地缓存 */
    fun refresh() {
        val account = _uiState.value.selectedAccount ?: return
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(refreshing = true, error = null)
            try {
                val data = mailRepository.refreshInbox(account.accountId, _uiState.value.boxType)
                _uiState.value = _uiState.value.copy(
                    refreshing = false,
                    noMore = data.list.size < PAGE_SIZE
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    refreshing = false,
                    error = friendlyMessage(e)
                )
            }
        }
    }

    /** 触底加载更早邮件 */
    fun loadMore() {
        if (loadMoreJob?.isActive == true) return
        val state = _uiState.value
        val account = state.selectedAccount ?: return
        if (state.noMore || state.emails.isEmpty() || state.loadingMore) return
        val cursor = state.emails.minOf { it.emailId }
        loadMoreJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loadingMore = true)
            try {
                val added = mailRepository.loadMore(account.accountId, state.boxType, cursor)
                _uiState.value = _uiState.value.copy(
                    loadingMore = false,
                    noMore = added < PAGE_SIZE
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(loadingMore = false, error = friendlyMessage(e))
            }
        }
    }

    fun markRead(emailId: Long) {
        viewModelScope.launch {
            try {
                mailRepository.markRead(listOf(emailId))
            } catch (_: Exception) {
                // 标记已读失败不打断用户
            }
        }
    }

    fun delete(ids: List<Long>) {
        viewModelScope.launch {
            try {
                mailRepository.delete(ids)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = friendlyMessage(e))
            }
        }
    }

    fun star(emailId: Long) {
        viewModelScope.launch {
            try {
                mailRepository.star(emailId)
            } catch (_: Exception) {
            }
        }
    }

    fun unstar(emailId: Long) {
        viewModelScope.launch {
            try {
                mailRepository.unstar(emailId)
            } catch (_: Exception) {
            }
        }
    }

    private fun friendlyMessage(e: Exception): String {
        val api = e as? com.cloudmail.app.network.ApiException
        return api?.message ?: (e.message ?: "网络异常，请稍后重试")
    }

    companion object {
        const val PAGE_SIZE = 30
        const val CACHE_LIMIT = 100
    }
}
