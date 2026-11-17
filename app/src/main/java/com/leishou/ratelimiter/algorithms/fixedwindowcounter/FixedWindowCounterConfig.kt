package com.leishou.ratelimiter.algorithms.fixedwindowcounter

import com.leishou.ratelimiter.model.LimitAlgorithmConfig

data class FixedWindowCounterConfig(
    val capacity: Int,
    val windowSize: Long): LimitAlgorithmConfig