package com.leishou.ratelimiter.algorithms.tokenbucket

import com.leishou.ratelimiter.model.LimitAlgorithmConfig

/*
    capacity: max token in bucket
    refillMillis: ms, total time to fully refill bucket
 */
data class TokenBucketConfig(
    val capacity: Int,
    val refillMillis: Long): LimitAlgorithmConfig