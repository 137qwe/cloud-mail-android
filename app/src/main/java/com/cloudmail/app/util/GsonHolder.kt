package com.cloudmail.app.util

import com.google.gson.Gson

/** 全局 Gson 实例：导航参数 JSON 序列化等 */
object GsonHolder {
    val gson: Gson = Gson()
}
