package com.cloudmail.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudmail.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val error: String? = null,
        val needManualLogin: Boolean = false,
        val registered: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /** 当前登录态（用于决定导航起始页） */
    val isLoggedIn: Boolean
        get() = authRepository.isLoggedInNow

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = UiState(loading = true)
            try {
                authRepository.login(email.trim(), password)
                _uiState.value = UiState()
            } catch (e: Exception) {
                _uiState.value = UiState(error = friendlyMessage(e))
            }
        }
    }

    fun register(email: String, password: String, code: String?) {
        viewModelScope.launch {
            _uiState.value = UiState(loading = true)
            try {
                val needManualLogin = authRepository.register(email.trim(), password, code?.takeIf { it.isNotBlank() })
                _uiState.value = UiState(registered = true, needManualLogin = needManualLogin)
            } catch (e: Exception) {
                _uiState.value = UiState(error = friendlyMessage(e))
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    private fun friendlyMessage(e: Exception): String {
        val api = e as? com.cloudmail.app.network.ApiException
        return api?.message ?: (e.message ?: "网络异常，请稍后重试")
    }
}
