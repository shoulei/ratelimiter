package com.leishou.ratelimiter.storage.memory

import com.leishou.ratelimiter.model.AlgorithmResult
import com.leishou.ratelimiter.model.LimitDecision
import com.leishou.ratelimiter.model.LimitKey
import com.leishou.ratelimiter.model.LimitState
import com.leishou.ratelimiter.storage.StorageInterface
import java.util.concurrent.ConcurrentHashMap

class InMemoryLimitStorage(): StorageInterface {
    private val states = ConcurrentHashMap<LimitKey, LimitState>()
    override fun update(key: LimitKey, updater: (LimitState?) -> AlgorithmResult): LimitDecision {
        var decision: LimitDecision = LimitDecision(true)
        states.compute(key) { _, oldState ->
            val result = updater(oldState)
            decision = result.decision
            result.state
        }

        return decision
    }
}