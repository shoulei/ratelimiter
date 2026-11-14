package com.leishou.ratelimiter.storage

import com.leishou.ratelimiter.model.AlgorithmResult
import com.leishou.ratelimiter.model.LimitDecision
import com.leishou.ratelimiter.model.LimitKey
import com.leishou.ratelimiter.model.LimitState

interface StorageInterface {
    fun update(key: LimitKey,
               updater: (LimitState?) -> AlgorithmResult): LimitDecision
}