package com.leishou.ratelimiter.algorithms.leakybucket

import com.leishou.ratelimiter.model.LimitState

data class LeakyBucketState(val water: Double, val lastLeakTimeMs: Long): LimitState