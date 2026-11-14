package com.leishou.ratelimiter.algorithms.tokenbucket

import com.leishou.ratelimiter.model.LimitState

data class TokenBucketState(val tokens: Double,
                            val lastRefillTime: Long): LimitState