package com.leishou.ratelimiter.algorithms.slidingwindow.counter

import com.leishou.ratelimiter.algorithms.slidingwindow.SlidingWindowConfig
import com.leishou.ratelimiter.model.AlgorithmInterface
import com.leishou.ratelimiter.model.AlgorithmResult
import com.leishou.ratelimiter.model.LimitAlgorithmConfig
import com.leishou.ratelimiter.model.LimitDecision
import com.leishou.ratelimiter.model.LimitState

/*
    The Sliding Window Counter algorithm is a rate-limiting technique that estimates rolling
    traffic volume by combining the current window's request count with a weighted fraction of
    the previous window's count
 */
class SlidingWindowCounterAlgorithm(): AlgorithmInterface {
    override fun evaluate(state: LimitState?, config: LimitAlgorithmConfig, now: Long): AlgorithmResult {
        require(config is SlidingWindowConfig) {
            "SlidingWindowCounterAlgorithm requires LimitAlgorithmConfig"
        }

        state?.let {
            require(it is SlidingWindowCounterState) {
                "SlidingWindowCounterAlgorithm requires SlidingWindowCounterState"
            }
        }

        require(config.capacity > 0) {
            "capacity must be greater than 0"
        }

        require(config.windowSize > 0) {
            "windowSize must be greater than 0"
        }

        val oldState = state as? SlidingWindowCounterState ?: SlidingWindowCounterState(0, 0, now)
        val elapsedMs = now - oldState.currentWindowStart
        val nextNewWindow = elapsedMs >= config.windowSize
        if (nextNewWindow) {
            val newElapsedMs = elapsedMs - config.windowSize
            val elapsedPercentage = newElapsedMs.toDouble() / config.windowSize
            val weightedCount = oldState.currentWindow * (1.0 - elapsedPercentage)
            val remainReq = config.capacity - weightedCount
            val allow = remainReq >= 1.0
            val remainReqAfter = if (allow) remainReq - 1.0 else remainReq.coerceAtLeast(0.0)
            val currentWindow = if (allow) 1 else 0
            val retry: Long? = if (allow) null else (config.windowSize * (1.0 - ((config.capacity - 1).toDouble() / oldState.currentWindow))).toLong() - newElapsedMs
            return AlgorithmResult(
                state = SlidingWindowCounterState(previousWindow = oldState.currentWindow, currentWindow = currentWindow, currentWindowStart = oldState.currentWindowStart + config.windowSize),
                decision = LimitDecision(isAllowed = allow, remaining = remainReqAfter.toInt(), retryAfterMs = retry)
            )
        } else {
            val elapsedPercentage = elapsedMs.toDouble() / config.windowSize
            val weightedCount = oldState.previousWindow * (1.0 - elapsedPercentage) + oldState.currentWindow
            val remainReq = config.capacity - weightedCount
            val allow = remainReq >= 1.0
            val remainReqAfter = if (allow) remainReq - 1.0 else remainReq.coerceAtLeast(0.0)
            val currentWindow = if (allow) oldState.currentWindow + 1 else oldState.currentWindow
            val retry: Long? = if (allow) null else (config.windowSize * (1.0 - ((config.capacity - 1 - oldState.currentWindow).toDouble() / oldState.previousWindow))).toLong() - elapsedMs
            return AlgorithmResult(
                state = SlidingWindowCounterState(previousWindow = oldState.previousWindow, currentWindow = currentWindow, currentWindowStart = oldState.currentWindowStart),
                decision = LimitDecision(isAllowed = allow, remaining = remainReqAfter.toInt(), retryAfterMs = retry)
            )
        }
    }
}