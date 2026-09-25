package com.cloudmail.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.cloudmail.app.data.repository.AuthRepository
import com.cloudmail.app.network.SessionEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

/**
 * 会话观察者：全局 401 事件 → 回到登录页。
 */
@HiltViewModel
class SessionObserverViewModel @Inject constructor(
    sessionEvents: SessionEvents,
    private val authRepository: AuthRepository
) : ViewModel() {
    val unauthorized: SharedFlow<Unit> = sessionEvents.unauthorized

    fun forceLogout() {
        authRepository.forceLogout()
    }
}
