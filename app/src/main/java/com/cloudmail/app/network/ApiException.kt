package com.cloudmail.app.network

/**
 * 业务异常：携带后端返回的 code（401/403/500…）与 message。
 */
class ApiException(
    val apiCode: Int,
    override val message: String
) : Exception(message)

/**
 * 统一解包 ApiResponse：code==200 返回 data，否则抛 ApiException。
 */
fun <T> unwrap(response: com.cloudmail.app.data.remote.dto.ApiResponse<T>): T? {
    if (response.code == 200) return response.data
    throw ApiException(response.code, response.message ?: "请求失败")
}
