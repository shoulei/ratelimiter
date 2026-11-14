package com.leishou.ratelimiter.model

data class ApiRequest(
    val userId: String?,
    val ipAddress: String?,
    val apiKey: String
)