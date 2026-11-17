package com.leishou.ratelimiter.algorithms.fixedwindowcounter

import com.leishou.ratelimiter.model.LimitState

data class FixedWindowCounterState(
    val current: Int,
    val currentWindowStart: Long): LimitState