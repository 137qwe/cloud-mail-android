package com.cloudmail.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ---------- 品牌色（浅色） ----------
val BrandBlue = Color(0xFF3B82F6)
val BrandCyan = Color(0xFF06B6D4)
val BrandDeep = Color(0xFF2563EB)
val Success = Color(0xFF10B981)
val Warning = Color(0xFFF59E0B)
val Error = Color(0xFFEF4444)

// ---------- 中性色（浅色） ----------
val BgLight = Color(0xFFF8FAFC)
val SurfaceLight = Color(0xFFFFFFFF)
val TextPrimaryLight = Color(0xFF0F172A)
val TextSecondaryLight = Color(0xFF64748B)
val DividerLight = Color(0xFFE2E8F0)

// ---------- 中性色（深色） ----------
val BgDark = Color(0xFF0F172A)
val SurfaceDark = Color(0xFF1E293B)
val TextPrimaryDark = Color(0xFFF1F5F9)
val TextSecondaryDark = Color(0xFF94A3B8)
val DividerDark = Color(0xFF334155)

// 品牌渐变：135°（Compose Brush 以起点→终点表达，与视觉方向一致）
val BrandGradient = Brush.linearGradient(
    colors = listOf(BrandBlue, BrandCyan)
)

// 深色模式降饱和变体
val BrandGradientDark = Brush.linearGradient(
    colors = listOf(Color(0xFF2563EB), Color(0xFF0891B2))
)

// 头像渐变：按 email hash 从色板取一组渐变，让每个账号有专属色相
fun avatarGradient(seed: String): Brush {
    val palette = listOf(
        listOf(BrandBlue, BrandCyan),
        listOf(Color(0xFF8B5CF6), BrandCyan),
        listOf(Warning, Error),
        listOf(Success, BrandBlue),
        listOf(Color(0xFFEC4899), Color(0xFF8B5CF6))
    )
    val index = seed.hashCode().let { if (it == Int.MIN_VALUE) 0 else kotlin.math.abs(it) } % palette.size
    return Brush.linearGradient(colors = palette[index])
}
