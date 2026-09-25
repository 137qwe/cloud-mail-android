package com.cloudmail.app.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 邮件本地缓存（最近一页），刷新/加载更多时以服务端为准覆盖。
 */
@Entity(tableName = "emails")
data class EmailEntity(
    @PrimaryKey val emailId: Long,
    val sendEmail: String?,
    val name: String?,
    val accountId: Long,
    val userId: Long,
    val subject: String?,
    val text: String?,
    val content: String?,
    val toEmail: String?,
    val toName: String?,
    val recipient: String?,
    val type: Int,
    val status: Int,
    val unread: Int,
    val isStar: Int,
    val createTime: String?
)
