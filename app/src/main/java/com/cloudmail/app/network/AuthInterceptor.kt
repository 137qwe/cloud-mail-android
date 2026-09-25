package com.cloudmail.app.network

import com.cloudmail.app.data.local.TokenStore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 全局 401 事件：任何接口认证过期/失效时发出，UI 层统一回到登录页。
 */
@Singleton
class SessionEvents @Inject constructor() {
    private val _unauthorized = MutableSharedFlow<Unit>(extraBufferCapacity = 4)
    val unauthorized = _unauthorized.asSharedFlow()

    fun onUnauthorized() {
        _unauthorized.tryEmit(Unit)
    }
}

/**
 * JWT 认证拦截器：Authorization = token，并携带 accept-language。
 * 401 时清理本地 token 并广播会话失效事件。
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore,
    private val sessionEvents: SessionEvents
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = tokenStore.token

        val newRequest = if (token.isNullOrBlank()) {
            request
        } else {
            request.newBuilder()
                .header("Authorization", token)
                .build()
        }.newBuilder()
            .header("accept-language", "zh")
            .build()

        val response = chain.proceed(newRequest)
        if (response.code == 401) {
            tokenStore.clear()
            sessionEvents.onUnauthorized()
        }
        return response
    }
}
