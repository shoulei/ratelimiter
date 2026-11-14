package com.leishou.ratelimiter.model

interface AlgorithmInterface {
    fun evaluate(state: LimitState?, config: LimitAlgorithmConfig, now: Long): AlgorithmResult
}