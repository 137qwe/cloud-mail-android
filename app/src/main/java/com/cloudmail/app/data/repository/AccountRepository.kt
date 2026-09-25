package com.cloudmail.app.data.repository

import com.cloudmail.app.data.remote.ApiService
import com.cloudmail.app.data.remote.dto.AccountAddRequest
import com.cloudmail.app.data.remote.dto.AccountDto
import com.cloudmail.app.data.remote.dto.AccountIdRequest
import com.cloudmail.app.data.remote.dto.SetNameRequest
import com.cloudmail.app.network.unwrap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val api: ApiService
) {
    /** 服务端单页上限 30，客户端一次拉全 */
    suspend fun list(size: Int = 30): List<AccountDto> =
        unwrap(api.accountList(size = size)) ?: emptyList()

    suspend fun add(email: String, token: String? = null): AccountDto =
        unwrap(api.accountAdd(AccountAddRequest(email, token))) ?: throw IllegalStateException("添加失败")

    suspend fun rename(accountId: Long, name: String) {
        unwrap(api.accountSetName(SetNameRequest(accountId, name)))
    }

    suspend fun setAllReceive(accountId: Long) {
        unwrap(api.accountSetAllReceive(AccountIdRequest(accountId)))
    }

    suspend fun setAsTop(accountId: Long) {
        unwrap(api.accountSetAsTop(AccountIdRequest(accountId)))
    }

    suspend fun delete(accountId: Long) {
        unwrap(api.accountDelete(accountId))
    }
}
