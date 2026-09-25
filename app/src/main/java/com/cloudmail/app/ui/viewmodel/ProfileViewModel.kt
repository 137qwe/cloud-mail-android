package com.cloudmail.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudmail.app.data.repository.UserRepository
import com.cloudmail.app.data.remote.dto.UserInfoDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    data class UiState(
        val userInfo: UserInfoDto? = null,
        val loading: Boolean = false,
        val changingPassword: Boolean = false,
        val error: String? = null,
        val showChangePwdDialog: Boolean = false
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
                val info = userRepository.loginUserInfo()
                _uiState.value = _uiState.value.copy(userInfo = info, loading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(loading = false, error = friendlyMessage(e))
            }
        }
    }

    fun showChangePwdDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showChangePwdDialog = show)
    }

    fun changePassword(password: String, onDone: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(changingPassword = true, error = null)
            try {
                userRepository.resetPassword(password)
                _uiState.value = _uiState.value.copy(changingPassword = false, showChangePwdDialog = false)
                onDone()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    changingPassword = false,
                    error = friendlyMessage(e)
                )
            }
        }
    }

    /** 判断是否管理员：permKeys 含 "*" */
    fun isAdmin(): Boolean = _uiState.value.userInfo?.permKeys?.contains("*") == true

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun friendlyMessage(e: Exception): String {
        val api = e as? com.cloudmail.app.network.ApiException
        return api?.message ?: (e.message ?: "网络异常，请稍后重试")
    }
}
