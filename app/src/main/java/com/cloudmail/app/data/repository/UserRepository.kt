package com.cloudmail.app.data.repository

import com.cloudmail.app.data.remote.ApiService
import com.cloudmail.app.data.remote.dto.ResetPasswordRequest
import com.cloudmail.app.data.remote.dto.UserInfoDto
import com.cloudmail.app.network.unwrap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun loginUserInfo(): UserInfoDto =
        unwrap(api.loginUserInfo()) ?: throw IllegalStateException("用户信息为空")

    suspend fun resetPassword(password: String) {
        unwrap(api.resetPassword(ResetPasswordRequest(password)))
    }
}
