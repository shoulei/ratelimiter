package com.leishou.ratelimiter.model

data class LimitDecision(
    val isAllowed: Boolean,
    val remaining: Int? = null,
    val retryAfterMs: Long? = null
)