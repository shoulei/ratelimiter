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
            "SlidingWindowCounterAlgorithm requires SlidingWindowConfig"
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
        // Determine how many complete windows have elapsed.
        val windowsPassed = elapsedMs / config.windowSize
        // Calculate elapsed time within the current window.
        val elapsedInCurrentWindow = elapsedMs % config.windowSize

        // Move the previous and current window counters forward.
        val (previousWindow, currentWindow) = when (windowsPassed) {
            0L -> {
                oldState.previousWindow to oldState.currentWindow
            }
            1L -> {
                oldState.currentWindow to 0
            }
            else -> {
                0 to 0
            }
        }

        // Calculate the weighted request count.
        val elapsedPercentage = elapsedInCurrentWindow.toDouble() / config.windowSize
        val weightedCount = previousWindow * (1.0 - elapsedPercentage) + currentWindow
        val remainingRequests = config.capacity - weightedCount
        val allowed = remainingRequests >= 1.0
        val newCurrentWindow = if (allowed) currentWindow + 1 else currentWindow
        val remaining = (remainingRequests - if (allowed) 1.0 else 0.0).coerceAtLeast(0.0).toInt()
        val retry = if (allowed) null else calculateRetryAfter(previousWindow, currentWindow, elapsedInCurrentWindow, config)
        return AlgorithmResult(
            state = SlidingWindowCounterState(previousWindow = previousWindow, currentWindow = newCurrentWindow, currentWindowStart = now - elapsedInCurrentWindow),
            decision = LimitDecision(isAllowed = allowed, remaining = remaining, retryAfterMs = retry)
        )
    }

    // elapsedMs must less than config.WindowSize
    private fun calculateRetryAfter(previousWindow: Int, currentWindow: Int,
                                    elapsedMs: Long, config: SlidingWindowConfig): Long {
        require(elapsedMs < config.windowSize) {
            "elapsedMs must be less than windowSize"
        }

        val max = config.capacity
        val winSize = config.windowSize
        val retryMs = if (currentWindow < max) {
            val expectedCount = max - currentWindow - 1
            val expectedMs = (1.0 - expectedCount.toDouble() / previousWindow) * winSize
            expectedMs.toLong() - elapsedMs
        } else {
            val remainingCurrentWindow = winSize - elapsedMs
            val expectedCount = currentWindow + 1 - max
            val nextWindowMs = expectedCount.toDouble() / max * winSize
            nextWindowMs.toLong() + remainingCurrentWindow
        }

        return retryMs
    }
}