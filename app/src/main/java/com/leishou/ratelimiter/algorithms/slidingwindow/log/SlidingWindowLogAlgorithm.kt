package com.leishou.ratelimiter.algorithms.slidingwindow.log

import com.leishou.ratelimiter.algorithms.slidingwindow.SlidingWindowConfig
import com.leishou.ratelimiter.model.AlgorithmInterface
import com.leishou.ratelimiter.model.AlgorithmResult
import com.leishou.ratelimiter.model.LimitAlgorithmConfig
import com.leishou.ratelimiter.model.LimitDecision
import com.leishou.ratelimiter.model.LimitState

/*
    The Sliding Window Log algorithm is a precise rate-limiting technique that stores the exact
    timestamp of every request and only allows new ones if the total count of timestamps falling
    within the moving look back window remains below the defined limit
    Here is standard implementation, NOT log rejected requests
 */
class SlidingWindowLogAlgorithm(): AlgorithmInterface {
    override fun evaluate(state: LimitState?, config: LimitAlgorithmConfig, now: Long): AlgorithmResult {
        require(config is SlidingWindowConfig) {
            "SlidingWindowLogAlgorithm requires LimitAlgorithmConfig"
        }

        state?.let {
            require(it is SlidingWindowLogState) {
                "SlidingWindowLogAlgorithm requires SlidingWindowLogState"
            }
        }

        require(config.capacity > 0) {
            "capacity must be greater than 0"
        }

        require(config.windowSize > 0) {
            "windowSize must be greater than 0"
        }

        val oldState = state as? SlidingWindowLogState?: SlidingWindowLogState(ArrayDeque())
        val timestamps = ArrayDeque(oldState.requestsTimestamps)
        val winSize = config.windowSize
        val capacity = config.capacity
        val windowStart = now - winSize
        // Remove expired requests from the sliding window.
        while (timestamps.isNotEmpty() &&
            timestamps.first() <= windowStart) {
            timestamps.removeFirst()
        }

        val requestCt = timestamps.size
        val allowed = requestCt < capacity

        // Only accepted requests consume quota.
        if (allowed) {
            timestamps.addLast(now)
        }
        val remaining = if (allowed) capacity - requestCt - 1 else 0
        val retry: Long? = calculateRetryAfter(timestamps, capacity, now - winSize)
        return AlgorithmResult(
            state = SlidingWindowLogState(timestamps),
            decision = LimitDecision(isAllowed = allowed, remaining = remaining, retryAfterMs = retry)
        )
    }

    private fun calculateRetryAfter(queue: ArrayDeque<Long>, capacity: Int, start: Long): Long? {
        if (queue.size < capacity)
            return null
        return queue[queue.size - capacity] - start
    }
}