package com.leishou.ratelimiter.algorithms.genericcellrate

import com.leishou.ratelimiter.model.LimitState

data class GenericCellRateState(
    val theoreticalArrivalTime: Long): LimitState