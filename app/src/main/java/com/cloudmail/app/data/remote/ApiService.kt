package com.cloudmail.app.data.remote

import com.cloudmail.app.data.remote.dto.AccountAddRequest
import com.cloudmail.app.data.remote.dto.AccountDto
import com.cloudmail.app.data.remote.dto.AccountIdRequest
import com.cloudmail.app.data.remote.dto.ApiResponse
import com.cloudmail.app.data.remote.dto.AttDto
import com.cloudmail.app.data.remote.dto.EmailDto
import com.cloudmail.app.data.remote.dto.EmailIdsRequest
import com.cloudmail.app.data.remote.dto.EmailListData
import com.cloudmail.app.data.remote.dto.LoginRequest
import com.cloudmail.app.data.remote.dto.LoginResult
import com.cloudmail.app.data.remote.dto.RegisterRequest
import com.cloudmail.app.data.remote.dto.RegisterResult
import com.cloudmail.app.data.remote.dto.ResetPasswordRequest
import com.cloudmail.app.data.remote.dto.SendEmailRequest
import com.cloudmail.app.data.remote.dto.SetNameRequest
import com.cloudmail.app.data.remote.dto.StarListData
import com.cloudmail.app.data.remote.dto.StarRequest
import com.cloudmail.app.data.remote.dto.UserInfoDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

/**
 * Cloud Mail Worker 后端接口（与 mail-worker/src/api 逐条对应，路径不含 /api 前缀）。
 */
interface ApiService {

    // ---------- 认证 ----------
    @POST("login")
    suspend fun login(@Body body: LoginRequest): ApiResponse<LoginResult>

    @POST("register")
    suspend fun register(@Body body: RegisterRequest): ApiResponse<RegisterResult>

    @DELETE("logout")
    suspend fun logout(): ApiResponse<Unit>

    // ---------- 我的 ----------
    @GET("my/loginUserInfo")
    suspend fun loginUserInfo(): ApiResponse<UserInfoDto>

    @PUT("my/resetPassword")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): ApiResponse<Unit>

    // ---------- 邮箱账号 ----------
    @GET("account/list")
    suspend fun accountList(
        @Query("accountId") accountId: Long? = null,
        @Query("size") size: Int? = null,
        @Query("lastSort") lastSort: Long? = null
    ): ApiResponse<List<AccountDto>>

    @POST("account/add")
    suspend fun accountAdd(@Body body: AccountAddRequest): ApiResponse<AccountDto>

    @DELETE("account/delete")
    suspend fun accountDelete(@Query("accountId") accountId: Long): ApiResponse<Unit>

    @PUT("account/setName")
    suspend fun accountSetName(@Body body: SetNameRequest): ApiResponse<Unit>

    @PUT("account/setAllReceive")
    suspend fun accountSetAllReceive(@Body body: AccountIdRequest): ApiResponse<Unit>

    @PUT("account/setAsTop")
    suspend fun accountSetAsTop(@Body body: AccountIdRequest): ApiResponse<Unit>

    // ---------- 邮件 ----------
    @GET("email/list")
    suspend fun emailList(
        @Query("emailId") emailId: Long? = null,
        @Query("type") type: Int? = null,
        @Query("accountId") accountId: Long? = null,
        @Query("size") size: Int? = null,
        @Query("timeSort") timeSort: Int? = null,
        @Query("allReceive") allReceive: Int? = null
    ): ApiResponse<EmailListData>

    @PUT("email/read")
    suspend fun emailRead(@Body body: EmailIdsRequest): ApiResponse<Unit>

    @DELETE("email/delete")
    suspend fun emailDelete(@Query("emailIds") emailIds: String): ApiResponse<Unit>

    @GET("email/attList")
    suspend fun emailAttList(@Query("emailId") emailId: Long): ApiResponse<List<AttDto>>

    @POST("email/send")
    suspend fun emailSend(@Body body: SendEmailRequest): ApiResponse<EmailDto>

    // ---------- 星标 ----------
    @POST("star/add")
    suspend fun starAdd(@Body body: StarRequest): ApiResponse<Unit>

    @GET("star/list")
    suspend fun starList(
        @Query("emailId") emailId: Long? = null,
        @Query("size") size: Int? = null
    ): ApiResponse<StarListData>

    @DELETE("star/cancel")
    suspend fun starCancel(@Query("emailId") emailId: Long): ApiResponse<Unit>
}
