package com.leishou.ratelimiter.algorithms.genericcellrate

import com.leishou.ratelimiter.model.LimitAlgorithmConfig

data class GenericCellRateConfig(
    val emissionIntervalMs: Long,
    val burstCapacity: Int): LimitAlgorithmConfig