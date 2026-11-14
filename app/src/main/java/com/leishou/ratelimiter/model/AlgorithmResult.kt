package com.leishou.ratelimiter.model

data class AlgorithmResult(
    val state: LimitState,
    val decision: LimitDecision
)
