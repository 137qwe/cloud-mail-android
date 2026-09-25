package com.cloudmail.app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {

    private const val DAY_MS = 24 * 60 * 60 * 1000L

    /**
     * 后端 createTime 形如 "2026-09-25 12:30:00" 或 ISO 时间。
     * 展示规则：今天 → HH:mm；昨天 → 昨天；今年 → M月d日；更早 → yyyy/MM/dd
     */
    fun format(createTime: String?, now: Long = System.currentTimeMillis()): String {
        if (createTime.isNullOrBlank()) return ""
        val time = parse(createTime) ?: return createTime
        val local = TimeZone.getDefault()
        val shifted = time + local.getOffset(time)
        val diff = now - time
        val todayStart = startOfDay(now)
        return when {
            time >= todayStart -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(shifted))
            time >= todayStart - DAY_MS -> "昨天"
            time >= startOfYear(now) -> SimpleDateFormat("M月d日", Locale.getDefault()).format(Date(shifted))
            else -> SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(shifted))
        }
    }

    private fun parse(raw: String): Long? {
        // 优先 ISO 格式
        return try {
            java.time.Instant.parse(raw).toEpochMilli()
        } catch (_: Exception) {
            try {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.parse(raw)?.time
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun startOfDay(ms: Long): Long {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = ms
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun startOfYear(ms: Long): Long {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = ms
        cal.set(java.util.Calendar.MONTH, 0)
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
