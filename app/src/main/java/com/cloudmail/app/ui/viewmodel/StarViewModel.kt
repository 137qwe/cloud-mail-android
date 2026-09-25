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

@HiltViewModel
class StarViewModel @Inject constructor(
    private val mailRepository: MailRepository
) : ViewModel() {

    data class UiState(
        val emails: List<EmailDto> = emptyList(),
        val loading: Boolean = false,
        val loadingMore: Boolean = false,
        val error: String? = null,
        val noMore: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var loadingJob: kotlinx.coroutines.Job? = null

    init {
        refresh()
    }

    fun refresh() {
        loadingJob?.cancel()
        loadingJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            try {
                val list = mailRepository.starList(cursor = null)
                _uiState.value = _uiState.value.copy(
                    emails = list,
                    loading = false,
                    noMore = list.size < PAGE_SIZE
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(loading = false, error = friendlyMessage(e))
            }
        }
    }

    fun loadMore() {
        if (loadingJob?.isActive == true) return
        val state = _uiState.value
        if (state.noMore || state.emails.isEmpty() || state.loadingMore) return
        val cursor = state.emails.minOf { it.emailId }
        loadingJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loadingMore = true)
            try {
                val more = mailRepository.starList(cursor = cursor)
                _uiState.value = _uiState.value.copy(
                    emails = state.emails + more,
                    loadingMore = false,
                    noMore = more.size < PAGE_SIZE
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(loadingMore = false, error = friendlyMessage(e))
            }
        }
    }

    fun unstar(emailId: Long) {
        viewModelScope.launch {
            try {
                mailRepository.unstar(emailId)
                _uiState.value = _uiState.value.copy(
                    emails = _uiState.value.emails.filterNot { it.emailId == emailId }
                )
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
    }
}
