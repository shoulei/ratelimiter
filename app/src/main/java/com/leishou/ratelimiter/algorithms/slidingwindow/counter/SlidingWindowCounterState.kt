package com.leishou.ratelimiter.algorithms.slidingwindow.counter

import com.leishou.ratelimiter.model.LimitState

/*
    previousWindow: previous window count
    currentWindow: current window count
 */
data class SlidingWindowCounterState(val previousWindow: Int,
                                val currentWindow: Int,
                                val currentWindowStart: Long): LimitState