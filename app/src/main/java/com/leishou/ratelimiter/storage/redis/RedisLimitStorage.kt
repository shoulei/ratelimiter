package com.leishou.ratelimiter.storage.redis

import com.leishou.ratelimiter.model.AlgorithmResult
import com.leishou.ratelimiter.model.LimitDecision
import com.leishou.ratelimiter.model.LimitKey
import com.leishou.ratelimiter.model.LimitState
import com.leishou.ratelimiter.storage.StorageInterface

class RedisLimitStorage(): StorageInterface {
    override fun update(key: LimitKey, updater: (LimitState?) -> AlgorithmResult): LimitDecision {
        // todo: implement
        return LimitDecision(false)
    }
}