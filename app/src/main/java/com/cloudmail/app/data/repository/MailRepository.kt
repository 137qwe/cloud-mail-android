package com.cloudmail.app.data.repository

import com.cloudmail.app.data.local.db.EmailDao
import com.cloudmail.app.data.local.db.EmailEntity
import com.cloudmail.app.data.remote.ApiService
import com.cloudmail.app.data.remote.dto.EmailDto
import com.cloudmail.app.data.remote.dto.EmailIdsRequest
import com.cloudmail.app.data.remote.dto.EmailListData
import com.cloudmail.app.data.remote.dto.SendEmailRequest
import com.cloudmail.app.data.remote.dto.StarRequest
import com.cloudmail.app.network.unwrap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MailRepository @Inject constructor(
    private val api: ApiService,
    private val emailDao: EmailDao
) {

    private fun EmailDto.toEntity(): EmailEntity = EmailEntity(
        emailId = emailId,
        sendEmail = sendEmail,
        name = name,
        accountId = accountId,
        userId = userId,
        subject = subject,
        text = text,
        content = content,
        toEmail = toEmail,
        toName = toName,
        recipient = recipient,
        type = type,
        status = status,
        unread = unread,
        isStar = isStar,
        createTime = createTime
    )

    /**
     * 刷新第一页：emailId=null → 服务端取最新（降序，timeSort=0）。
     * 返回最新一页，并替换本地缓存。
     */
    suspend fun refreshInbox(accountId: Long, type: Int, size: Int = 30): EmailListData {
        val data = unwrap(
            api.emailList(
                emailId = null,
                type = type,
                accountId = accountId,
                size = size,
                timeSort = 0
            )
        ) ?: EmailListData()
        emailDao.clearByAccount(accountId)
        emailDao.upsertAll(data.list.map { it.toEntity() })
        return data
    }

    /**
     * 加载更早邮件：以当前列表最小 emailId 为游标（降序），结果追加进本地缓存。
     * @return 本次新增数量
     */
    suspend fun loadMore(accountId: Long, type: Int, cursor: Long, size: Int = 30): Int {
        val data = unwrap(
            api.emailList(
                emailId = cursor,
                type = type,
                accountId = accountId,
                size = size,
                timeSort = 0
            )
        ) ?: EmailListData()
        emailDao.upsertAll(data.list.map { it.toEntity() })
        return data.list.size
    }

    suspend fun markRead(ids: List<Long>) {
        if (ids.isEmpty()) return
        emailDao.updateUnread(ids, 0)
        unwrap(api.emailRead(EmailIdsRequest(ids)))
    }

    suspend fun delete(ids: List<Long>) {
        if (ids.isEmpty()) return
        emailDao.markDeleted(ids)
        unwrap(api.emailDelete(ids.joinToString(",")))
    }

    suspend fun star(emailId: Long) {
        emailDao.updateStar(listOf(emailId), 1)
        unwrap(api.starAdd(StarRequest(emailId)))
    }

    suspend fun unstar(emailId: Long) {
        emailDao.updateStar(listOf(emailId), 0)
        unwrap(api.starCancel(emailId))
    }

    /** 星标列表（不做本地缓存，直接网络） */
    suspend fun starList(cursor: Long?, size: Int = 30): List<EmailDto> =
        unwrap(api.starList(cursor, size))?.list ?: emptyList()

    suspend fun send(request: SendEmailRequest): EmailDto =
        unwrap(api.emailSend(request)) ?: throw IllegalStateException("发送失败，服务端无返回")
}
