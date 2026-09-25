package com.cloudmail.app.util

/**
 * 邮件正文相关工具：纯文本 → 简单 HTML、邮件地址提取。
 */
object HtmlUtils {

    /**
     * 纯文本正文转简单 HTML（换行 → <br>，空格转义），供 WebView 展示。
     */
    fun textToHtml(text: String?): String {
        if (text.isNullOrBlank()) return "<p></p>"
        val escaped = text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
        return "<html><body style=\"font-size:15px;color:#0f172a;line-height:1.7;\">" +
            escaped.replace("\n", "<br/>") +
            "</body></html>"
    }

    /**
     * 从地址中提取 name 或 email 前缀作为展示名。
     * 形如 "John <john@x.com>" 或 "john@x.com"。
     */
    fun displayName(raw: String?): String {
        if (raw.isNullOrBlank()) return "未知"
        val match = Regex("^([^<]+?)\\s*<(.+)>$").find(raw.trim())
        if (match != null) {
            val name = match.groupValues[1].trim()
            if (name.isNotBlank()) return name
            return emailName(match.groupValues[2])
        }
        return emailName(raw.trim())
    }

    fun emailName(email: String): String = email.substringBefore("@")

    /** 回复引用块 */
    fun quoteHtml(original: EmailQuoteSource?): String {
        if (original == null) return ""
        return "<br/><br/><div style=\"border-left:3px solid #cbd5e1;padding-left:12px;color:#64748b;font-size:13px;\">" +
            "------------------ 原始邮件 ------------------<br/>" +
            "发件人：${original.from}<br/>时间：${original.time}<br/>主题：${original.subject}<br/><br/>" +
            "${original.body}</div>"
    }
}

data class EmailQuoteSource(
    val from: String,
    val time: String,
    val subject: String,
    val body: String
)
