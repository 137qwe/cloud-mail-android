package com.cloudmail.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cloudmail.app.data.remote.dto.EmailDto
import com.cloudmail.app.data.remote.dto.EmailType
import com.cloudmail.app.util.DateUtils
import com.cloudmail.app.util.HtmlUtils

/**
 * 邮件列表项：渐变首字母头像 + 发件人/主题/摘要 三行 + 时间 + 未读圆点 + 星标。
 */
@Composable
fun EmailListItem(
    email: EmailDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val unread = email.unread == 1 && email.type == EmailType.RECEIVE
    val sender = when (email.type) {
        EmailType.SEND -> email.toEmail ?: email.sendEmail.orEmpty()
        else -> email.sendEmail ?: ""
    }
    val displayName = HtmlUtils.displayName(sender)
    val subject = email.subject.orEmpty()
    val preview = if (email.type == EmailType.SEND) {
        "发送给 ${HtmlUtils.displayName(email.toEmail ?: "")}"
    } else {
        email.text?.replace('\n', ' ') ?: ""
    }
    val time = DateUtils.format(email.createTime)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = if (unread) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
        } else {
            MaterialTheme.colorScheme.surface
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarInitial(
                email = email.sendEmail ?: sender,
                name = displayName,
                size = 40
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (unread) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (email.isStar == 1) {
                        Text("★", color = MaterialTheme.colorScheme.tertiary, fontSize = 14.sp)
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        text = time,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = subject,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 3.dp)
                )
                Text(
                    text = preview,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            if (unread) {
                Box(
                    Modifier
                        .padding(start = 10.dp)
                        .size(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                )
            }
        }
    }
}
