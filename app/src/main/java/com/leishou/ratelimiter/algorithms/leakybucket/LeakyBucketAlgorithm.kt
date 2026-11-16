package com.leishou.ratelimiter.algorithms.leakybucket

import com.leishou.ratelimiter.model.AlgorithmInterface
import com.leishou.ratelimiter.model.AlgorithmResult
import com.leishou.ratelimiter.model.LimitAlgorithmConfig
import com.leishou.ratelimiter.model.LimitDecision
import com.leishou.ratelimiter.model.LimitState
import kotlin.math.ceil

/*
    Requests enter a First-In, First-Out (FIFO) queue of a fixed capacity and are processed at a
    constant, steady rate. If the queue is full, new requests are dropped
 */
class LeakyBucketAlgorithm(): AlgorithmInterface {
    override fun evaluate(state: LimitState?, config: LimitAlgorithmConfig, now: Long): AlgorithmResult {
        require(config is LeakyBucketConfig) {
            "LeakyBucketAlgorithm requires LeakyBucketConfig"
        }

        state?.let {
            require(it is LeakyBucketState) {
                "LeakyBucketAlgorithm requires LeakyBucketState"
            }
        }

        require(config.capacity > 0) {
            "capacity must be greater than 0"
        }

        require(config.leakRatePerMs > 0) {
            "leakRateInMs must be greater than 0"
        }

        val oldState = state as? LeakyBucketState?: LeakyBucketState(0.0, now)
        val elapsedMs = (now - oldState.lastLeakTimeMs).coerceAtLeast(0L)
        val leaked = elapsedMs * config.leakRatePerMs
        val water = (oldState.water - leaked).coerceAtLeast(0.0)
        val allowed = water + 1 <= config.capacity
        val newWater = water + (if (allowed) 1.0 else 0.0)
        val remainingCapacity = config.capacity - water - (if (allowed) 1.0 else 0.0)
        val retry: Long? = if (allowed) null else ceil((water + 1 - config.capacity) / config.leakRatePerMs).toLong()

        return AlgorithmResult(
            state = LeakyBucketState(water = newWater, lastLeakTimeMs = now),
            decision = LimitDecision(isAllowed = allowed, remaining = remainingCapacity.toInt(), retryAfterMs = retry)
        )
    }
}