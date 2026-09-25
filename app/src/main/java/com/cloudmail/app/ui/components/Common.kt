package com.cloudmail.app.ui.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cloudmail.app.ui.theme.BrandCyan
import com.cloudmail.app.ui.theme.BrandGradient
import com.cloudmail.app.ui.theme.BrandBlue
import com.cloudmail.app.ui.theme.avatarGradient

/** 渐变主按钮：品牌渐变底 + 白字，14dp 圆角 */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    val background = if (enabled && !loading) {
        BrandGradient
    } else {
        Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceVariant))
    }
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .clickable(enabled = enabled && !loading) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = androidx.compose.ui.graphics.Color.White
            )
        } else {
            Text(
                text,
                color = androidx.compose.ui.graphics.Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }
    }
}

/** 发件人首字母渐变头像 */
@Composable
fun AvatarInitial(
    email: String?,
    name: String?,
    modifier: Modifier = Modifier,
    size: Int = 40
) {
    val initial = (name ?: com.cloudmail.app.util.HtmlUtils.emailName(email ?: ""))
        .take(1)
        .uppercase()
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(avatarGradient(email ?: "unknown")),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            color = androidx.compose.ui.graphics.Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size * 0.4f).sp
        )
    }
}

/** 毛玻璃顶栏：浅色半透明白 + 轻阴影 */
@Composable
fun GlassTopBar(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            actions()
        }
    }
}

/** 空状态 */
@Composable
fun EmptyState(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(listOf(BrandBlue.copy(alpha = 0.12f), BrandCyan.copy(alpha = 0.12f)))),
            contentAlignment = Alignment.Center
        ) {
            Text("☁", fontSize = 34.sp)
        }
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp)
        )
        if (subtitle != null) {
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
        action?.invoke()
    }
}

/** 加载占位 */
@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.dp
        )
    }
}

/** 列表项骨架屏 */
@Composable
fun SkeletonListItem(modifier: Modifier = Modifier) {
    val shimmer = androidx.compose.animation.core.rememberInfiniteTransition(label = "skeleton")
    val alpha by shimmer.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.8f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(900)
        ),
        label = "alpha"
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha))
        )
        Column(Modifier.padding(start = 12.dp).weight(1f)) {
            Box(
                Modifier
                    .fillMaxWidth(0.4f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha))
            )
            Box(
                Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(0.9f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha))
            )
        }
    }
}

/** 渐变 FAB（写信入口） */
@Composable
fun GradientFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(28.dp))
            .size(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(BrandGradient)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text("✉", color = androidx.compose.ui.graphics.Color.White, fontSize = 24.sp)
    }
}
