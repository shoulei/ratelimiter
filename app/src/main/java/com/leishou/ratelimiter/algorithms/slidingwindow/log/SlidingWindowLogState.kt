package com.leishou.ratelimiter.algorithms.slidingwindow.log

import com.leishou.ratelimiter.model.LimitState

data class SlidingWindowLogState(val requestsTimestamps: ArrayDeque<Long>): LimitState