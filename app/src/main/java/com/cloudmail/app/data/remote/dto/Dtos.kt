package com.cloudmail.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * 后端统一响应体：{ code, message, data }，code=200 成功。
 */
data class ApiResponse<T>(
    val code: Int = 0,
    val message: String? = null,
    val data: T? = null
)

/** 邮件类型：0=接收，1=发送 */
object EmailType {
    const val RECEIVE = 0
    const val SEND = 1
}

/** 邮件状态：0接收 1已发 2已送达 3退信 4投诉 5延迟 6保存 7无收件人 8失败 */
object EmailStatus {
    const val RECEIVE = 0
    const val SENT = 1
    const val DELIVERED = 2
    const val BOUNCED = 3
    const val COMPLAINED = 4
    const val DELAYED = 5
    const val SAVING = 6
    const val NOONE = 7
    const val FAILED = 8
}

data class LoginResult(val token: String? = null)

data class RegisterResult(@SerializedName("regVerifyOpen") val regVerifyOpen: Boolean = false)

data class RoleDto(
    val roleId: Long? = null,
    val name: String? = null,
    val sendType: String? = null,
    val sendCount: Int? = null,
    val accountCount: Int? = null,
    val isDefault: Int? = null,
    val createTime: String? = null
)

data class AccountDto(
    val accountId: Long = 0,
    val email: String? = null,
    val name: String? = null,
    val status: Int = 0,
    val latestEmailTime: String? = null,
    val createTime: String? = null,
    val userId: Long = 0,
    @SerializedName("allReceive") val allReceive: Int = 0,
    val sort: Int = 0,
    val isDel: Int = 0
)

data class UserInfoDto(
    val userId: Long = 0,
    val sendCount: Int = 0,
    val email: String? = null,
    val name: String? = null,
    val permKeys: List<String>? = null,
    val role: RoleDto? = null,
    val type: Long? = null,
    val account: AccountDto? = null
)

data class AttDto(
    val attId: Long = 0,
    val userId: Long = 0,
    val emailId: Long = 0,
    val accountId: Long = 0,
    val key: String? = null,
    val filename: String? = null,
    val mimeType: String? = null,
    val size: Long? = null,
    val status: Int = 0,
    val type: Int = 0,
    val disposition: String? = null,
    val related: String? = null,
    val contentId: String? = null,
    val encoding: String? = null,
    val createTime: String? = null
)

data class EmailDto(
    val emailId: Long = 0,
    val sendEmail: String? = null,
    val name: String? = null,
    val accountId: Long = 0,
    val userId: Long = 0,
    val subject: String? = null,
    val code: String? = null,
    val text: String? = null,
    val content: String? = null,
    val cc: String? = null,
    val bcc: String? = null,
    val recipient: String? = null,
    val toEmail: String? = null,
    val toName: String? = null,
    val inReplyTo: String? = null,
    val relation: String? = null,
    val messageId: String? = null,
    val type: Int = 0,
    val status: Int = 0,
    val resendEmailId: String? = null,
    val message: String? = null,
    val unread: Int = 0,
    val createTime: String? = null,
    val isDel: Int = 0,
    @SerializedName("isStar") val isStar: Int = 0,
    val starId: Long? = null,
    val attList: List<AttDto>? = null
)

data class EmailListData(
    val list: List<EmailDto> = emptyList(),
    val total: Long = 0,
    val latestEmail: EmailDto? = null
)

/** star/list 返回包装：{ list } */
data class StarListData(
    val list: List<EmailDto> = emptyList()
)

// ---------- 请求体 ----------

data class LoginRequest(val email: String, val password: String)

data class RegisterRequest(
    val email: String,
    val password: String,
    val code: String? = null,
    val token: String? = null
)

/** email/read 的 body：{ emailIds: number[] } */
data class EmailIdsRequest(val emailIds: List<Long>)

data class StarRequest(val emailId: Long)

data class AccountAddRequest(val email: String, val token: String? = null)

data class AccountIdRequest(val accountId: Long)

data class SetNameRequest(val accountId: Long, val name: String)

data class ResetPasswordRequest(val password: String)

data class AttachmentDto(
    val content: String,
    val filename: String,
    val size: Long,
    val contentType: String
)

data class SendEmailRequest(
    val accountId: Long,
    val name: String? = null,
    val sendType: String? = null,
    val emailId: Long? = null,
    val receiveEmail: List<String>,
    val text: String? = null,
    val content: String? = null,
    val subject: String,
    val attachments: List<AttachmentDto> = emptyList()
)
