package com.cloudmail.app.data.repository

import com.cloudmail.app.data.local.TokenStore
import com.cloudmail.app.data.remote.ApiService
import com.cloudmail.app.data.remote.dto.LoginRequest
import com.cloudmail.app.data.remote.dto.RegisterRequest
import com.cloudmail.app.network.unwrap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStore: TokenStore
) {
    private val _isLoggedIn = MutableStateFlow(!tokenStore.token.isNullOrBlank())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    val isLoggedInNow: Boolean get() = _isLoggedIn.value

    suspend fun login(email: String, password: String) {
        val result = unwrap(api.login(LoginRequest(email, password)))
        val token = result?.token
        require(!token.isNullOrBlank()) { "登录响应缺少 token" }
        tokenStore.token = token
        _isLoggedIn.value = true
    }

    /** 注册成功后若无需人机验证，自动登录。返回是否需要人工登录（注册码/验证分支）。 */
    suspend fun register(email: String, password: String, code: String?): Boolean {
        val result = unwrap(api.register(RegisterRequest(email, password, code)))
        val regVerifyOpen = result?.regVerifyOpen ?: false
        if (!regVerifyOpen) {
            // 注册成功且未开启验证，直接自动登录
            login(email, password)
        }
        return regVerifyOpen
    }

    suspend fun logout() {
        try {
            unwrap(api.logout())
        } catch (_: Exception) {
            // 服务端登出失败不阻塞本地清理
        } finally {
            tokenStore.clear()
            _isLoggedIn.value = false
        }
    }

    fun forceLogout() {
        tokenStore.clear()
        _isLoggedIn.value = false
    }
}
