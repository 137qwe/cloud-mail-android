package com.cloudmail.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudmail.app.data.repository.AccountRepository
import com.cloudmail.app.data.repository.MailRepository
import com.cloudmail.app.data.remote.dto.AccountDto
import com.cloudmail.app.data.remote.dto.SendEmailRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 写信：支持新写 / 回复(reply) / 转发(forward)。按产品裁剪，不包含附件上传。
 */
@HiltViewModel
class ComposeViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val mailRepository: MailRepository
) : ViewModel() {

    data class UiState(
        val accounts: List<AccountDto> = emptyList(),
        val selectedAccountId: Long = 0L,
        val sending: Boolean = false,
        val sent: Boolean = false,
        val error: String? = null,
        val prefillRecipients: List<String> = emptyList(),
        val prefillSubject: String = "",
        val prefillContent: String = "",
        val sendType: String? = null,
        val replyEmailId: Long? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadAccounts(prefill: ComposePrefill?) {
        if (_uiState.value.accounts.isNotEmpty()) return
        viewModelScope.launch {
            try {
                val accounts = accountRepository.list()
                _uiState.value = _uiState.value.copy(
                    accounts = accounts,
                    selectedAccountId = prefill?.accountId ?: accounts.firstOrNull()?.accountId ?: 0L,
                    prefillRecipients = prefill?.recipients ?: emptyList(),
                    prefillSubject = prefill?.subject ?: "",
                    prefillContent = prefill?.content ?: "",
                    sendType = prefill?.sendType,
                    replyEmailId = prefill?.replyEmailId
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "加载账号失败")
            }
        }
    }

    fun selectAccount(accountId: Long) {
        _uiState.value = _uiState.value.copy(selectedAccountId = accountId)
    }

    fun send(
        recipients: List<String>,
        subject: String,
        text: String,
        onDone: () -> Unit
    ) {
        val state = _uiState.value
        if (state.selectedAccountId == 0L || recipients.isEmpty()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(sending = true, error = null)
            try {
                val html = com.cloudmail.app.util.HtmlUtils.textToHtml(text)
                val request = SendEmailRequest(
                    accountId = state.selectedAccountId,
                    sendType = state.sendType,
                    emailId = state.replyEmailId,
                    receiveEmail = recipients,
                    text = text,
                    content = html,
                    subject = subject.trim(),
                    attachments = emptyList() // 产品裁剪：不支持附件上传
                )
                mailRepository.send(request)
                _uiState.value = _uiState.value.copy(sending = false, sent = true)
                onDone()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    sending = false,
                    error = (e as? com.cloudmail.app.network.ApiException)?.message ?: e.message
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/** 写信预填信息（回复/转发） */
data class ComposePrefill(
    val accountId: Long = 0L,
    val recipients: List<String> = emptyList(),
    val subject: String = "",
    val content: String = "",
    val sendType: String? = null,
    val replyEmailId: Long? = null
) {
    companion object {
        const val TYPE_REPLY = "reply"
        const val TYPE_FORWARD = "forward"
    }
}
