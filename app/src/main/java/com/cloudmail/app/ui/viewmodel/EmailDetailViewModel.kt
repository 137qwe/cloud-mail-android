package com.cloudmail.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudmail.app.data.repository.MailRepository
import com.cloudmail.app.data.remote.dto.EmailDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 邮件详情：解析导航传入的 EmailDto JSON，处理已读、删除、星标。
 */
@HiltViewModel
class EmailDetailViewModel @Inject constructor(
    private val mailRepository: MailRepository
) : ViewModel() {

    data class UiState(
        val email: EmailDto? = null,
        val markedRead: Boolean = false,
        val deleting: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var emailId: Long = 0L

    fun init(emailJson: String) {
        if (_uiState.value.email != null) return
        val email = runCatching {
            com.cloudmail.app.util.GsonHolder.gson.fromJson(emailJson, EmailDto::class.java)
        }.getOrNull()
        emailId = email?.emailId ?: 0L
        _uiState.value = UiState(email = email)
        if (email != null && email.unread == 1 && email.type == com.cloudmail.app.data.remote.dto.EmailType.RECEIVE) {
            markRead()
        }
    }

    fun markRead() {
        if (_uiState.value.markedRead || emailId == 0L) return
        viewModelScope.launch {
            try {
                mailRepository.markRead(listOf(emailId))
                _uiState.value = _uiState.value.copy(markedRead = true)
                _uiState.value.email?.let { email ->
                    _uiState.value = _uiState.value.copy(
                        email = email.copy(unread = 0)
                    )
                }
            } catch (_: Exception) {
            }
        }
    }

    fun delete(onDeleted: () -> Unit) {
        if (emailId == 0L) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(deleting = true)
            try {
                mailRepository.delete(listOf(emailId))
                onDeleted()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(deleting = false, error = e.message)
            }
        }
    }

    fun toggleStar() {
        val email = _uiState.value.email ?: return
        viewModelScope.launch {
            try {
                if (email.isStar == 1) {
                    mailRepository.unstar(email.emailId)
                    _uiState.value = _uiState.value.copy(email = email.copy(isStar = 0))
                } else {
                    mailRepository.star(email.emailId)
                    _uiState.value = _uiState.value.copy(email = email.copy(isStar = 1))
                }
            } catch (_: Exception) {
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
