package com.leishou.ratelimiter.algorithms.leakybucket

import com.leishou.ratelimiter.model.LimitAlgorithmConfig

/*
    capacity: max water in bucket
    leakRateInMs: water leaked in every ms
 */
data class LeakyBucketConfig(
    val capacity: Int,
    val leakRatePerMs: Double): LimitAlgorithmConfig