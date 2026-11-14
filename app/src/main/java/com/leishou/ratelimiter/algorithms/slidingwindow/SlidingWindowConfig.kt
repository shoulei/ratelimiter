package com.leishou.ratelimiter.algorithms.slidingwindow

import com.leishou.ratelimiter.model.LimitAlgorithmConfig

/*
    capacity: max request count in windowSize
    windowSize: ms, windows size, ms
 */
class SlidingWindowConfig(
    val capacity: Int,
    val windowSize: Long): LimitAlgorithmConfig