package com.cloudmail.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudmail.app.data.repository.AccountRepository
import com.cloudmail.app.data.remote.dto.AccountDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val accountRepository: AccountRepository
) : ViewModel() {

    data class UiState(
        val accounts: List<AccountDto> = emptyList(),
        val loading: Boolean = false,
        val error: String? = null,
        val showAddDialog: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            try {
                val accounts = accountRepository.list()
                _uiState.value = _uiState.value.copy(accounts = accounts, loading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(loading = false, error = friendlyMessage(e))
            }
        }
    }

    fun showAddDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAddDialog = show)
    }

    fun add(email: String) {
        viewModelScope.launch {
            try {
                accountRepository.add(email.trim())
                load()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = friendlyMessage(e))
            }
        }
    }

    fun rename(accountId: Long, name: String) {
        viewModelScope.launch {
            try {
                accountRepository.rename(accountId, name.trim())
                load()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = friendlyMessage(e))
            }
        }
    }

    fun setAllReceive(accountId: Long) {
        viewModelScope.launch {
            try {
                accountRepository.setAllReceive(accountId)
                load()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = friendlyMessage(e))
            }
        }
    }

    fun setAsTop(accountId: Long) {
        viewModelScope.launch {
            try {
                accountRepository.setAsTop(accountId)
                load()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = friendlyMessage(e))
            }
        }
    }

    fun delete(accountId: Long) {
        viewModelScope.launch {
            try {
                accountRepository.delete(accountId)
                load()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = friendlyMessage(e))
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun friendlyMessage(e: Exception): String {
        val api = e as? com.cloudmail.app.network.ApiException
        return api?.message ?: (e.message ?: "网络异常，请稍后重试")
    }
}
