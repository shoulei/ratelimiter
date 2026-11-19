package com.leishou.ratelimiter.algorithms.genericcellrate

import com.leishou.ratelimiter.model.AlgorithmInterface
import com.leishou.ratelimiter.model.AlgorithmResult
import com.leishou.ratelimiter.model.LimitAlgorithmConfig
import com.leishou.ratelimiter.model.LimitDecision
import com.leishou.ratelimiter.model.LimitState

/*
    Virtual scheduling rate limiter that enforces a steady request rate while allowing controlled
    bursts, requiring only a single timestamp (TAT) per user

    • Emission Interval (T): The expected time gap between consecutive requests at a steady rate.
    For example, a limit of 10 requests per second gives an emission interval of T = 0.1s (100ms).

    • Delay Variation Tolerance (τ): The burst allowance expressed as a time buffer. If you allow
    a burst of B extra requests, the tolerance is calculated as τ = B * T.

    • Theoretical Arrival Time (TAT): The single timestamp stored in memory or Redis for each user.
    It represents when the next request should arrive if traffic is flowing at the exact steady rate.
 */
class GenericCellRateAlgorithm(): AlgorithmInterface {
    override fun evaluate(state: LimitState?, config: LimitAlgorithmConfig, now: Long): AlgorithmResult {
        require(config is GenericCellRateConfig) {
            "GenericCellRateAlgorithm requires GenericCellRateConfig"
        }

        state?.let {
            require(it is GenericCellRateState) {
                "GenericCellRateAlgorithm requires GenericCellRateState"
            }
        }

        require(config.burstCapacity > 0) {
            "burstCapacity must be greater than 0"
        }

        require(config.emissionIntervalMs > 0) {
            "emissionIntervalMs must be greater than 0"
        }

        val oldState = state as? GenericCellRateState?: GenericCellRateState(now)
        val interval = config.emissionIntervalMs
        val tolerance = (config.burstCapacity.toLong() - 1) * interval
        val allowAt = oldState.theoreticalArrivalTime - tolerance
        val allowed = now >= allowAt
        val newTat = if (allowed) maxOf(now, oldState.theoreticalArrivalTime) + interval else oldState.theoreticalArrivalTime
        val remaining = if (allowed) {
            val nextAllowAt = newTat - tolerance
            if (now < nextAllowAt) {
                0
            } else {
                ((now - nextAllowAt) / interval + 1).toInt()
                    .coerceAtMost(config.burstCapacity)
            }
        } else {
            0
        }
        val retry: Long? = if (allowed) null else allowAt - now
        return AlgorithmResult(
            state = GenericCellRateState(newTat),
            decision = LimitDecision(isAllowed = allowed, remaining = remaining, retryAfterMs = retry)
        )
    }
}