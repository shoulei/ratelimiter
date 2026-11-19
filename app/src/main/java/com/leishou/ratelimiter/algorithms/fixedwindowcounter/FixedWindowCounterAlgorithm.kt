package com.leishou.ratelimiter.algorithms.fixedwindowcounter

import com.leishou.ratelimiter.model.AlgorithmInterface
import com.leishou.ratelimiter.model.AlgorithmResult
import com.leishou.ratelimiter.model.LimitAlgorithmConfig
import com.leishou.ratelimiter.model.LimitDecision
import com.leishou.ratelimiter.model.LimitState

/*
    Fixed Window Counter algorithm divides time into fixed, non-overlapping intervals (windows) and
    uses a single counter to track the number of requests within the current window. If the counter
    exceeds a predefined threshold, new requests are rejected until the next time window begins and
    the counter resets to zero.
 */
class FixedWindowCounterAlgorithm(): AlgorithmInterface {
    override fun evaluate(state: LimitState?, config: LimitAlgorithmConfig, now: Long): AlgorithmResult {
        require(config is FixedWindowCounterConfig) {
            "FixedWindowCounterAlgorithm requires FixedWindowCounterConfig"
        }

        state?.let {
            require(it is FixedWindowCounterState) {
                "FixedWindowCounterAlgorithm requires FixedWindowCounterState"
            }
        }

        require(config.capacity > 0) {
            "capacity must be greater than 0"
        }

        require(config.windowSize > 0) {
            "windowSize must be greater than 0"
        }

        val initialWindowStart = (now / config.windowSize) * config.windowSize
        val oldState = state as? FixedWindowCounterState?: FixedWindowCounterState(0, initialWindowStart)
        val elapsed = (now - oldState.currentWindowStart).coerceAtLeast(0L)
        val windowsPassed = elapsed / config.windowSize
        val windowStart = oldState.currentWindowStart + windowsPassed * config.windowSize

        val current = if (windowsPassed == 0L) oldState.current else 0

        val allowed = current < config.capacity
        val newCurrent = current + if (allowed) 1 else 0
        val remaining = (config.capacity - newCurrent).coerceAtLeast(0)
        val retry: Long? = if (allowed) null else windowStart + config.windowSize - now
        return AlgorithmResult(
            state = FixedWindowCounterState(current = newCurrent, currentWindowStart = windowStart),
            decision = LimitDecision(isAllowed = allowed, remaining = remaining, retryAfterMs = retry)
        )
    }
}