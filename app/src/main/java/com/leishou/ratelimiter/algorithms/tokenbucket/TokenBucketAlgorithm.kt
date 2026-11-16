package com.leishou.ratelimiter.algorithms.tokenbucket

import com.leishou.ratelimiter.model.AlgorithmInterface
import com.leishou.ratelimiter.model.AlgorithmResult
import com.leishou.ratelimiter.model.LimitAlgorithmConfig
import com.leishou.ratelimiter.model.LimitDecision
import com.leishou.ratelimiter.model.LimitState
import kotlin.math.ceil

/*
    The Token Bucket Algorithm is a rate-limiting and traffic-shaping method that allows data packets
    or API requests to pass through only when they can consume a token from a virtual bucket that
    refills at a steady rate up to a maximum capacity
 */
class TokenBucketAlgorithm(): AlgorithmInterface {
    override fun evaluate(state: LimitState?, config: LimitAlgorithmConfig, now: Long): AlgorithmResult {
        require(config is TokenBucketConfig) {
           "TokenBucketAlgorithm requires TokenBucketConfig"
        }

        state?.let {
            require(it is TokenBucketState) {
                "TokenBucketAlgorithm requires TokenBucketState"
            }
        }

        require(config.capacity > 0) {
            "capacity must be greater than 0"
        }

        require(config.refillMillis > 0) {
            "refillMillis must be greater than 0"
        }

        val refillRate = config.capacity.toDouble() / config.refillMillis
        val oldState = state as? TokenBucketState?: TokenBucketState(config.capacity.toDouble(), now)
        val elapsedTime  = (now - oldState.lastRefillTime).coerceAtLeast(0L)
        val tokenNow = (oldState.tokens + elapsedTime * refillRate).coerceAtMost(config.capacity.toDouble())
        val allow = tokenNow >= 1.0
        val remainToken = if (allow) tokenNow - 1.0 else tokenNow
        val retry: Long? = if (allow) null else ceil((1 - tokenNow) / refillRate).toLong()

        return AlgorithmResult(
            state = TokenBucketState(tokens = remainToken, lastRefillTime = now),
            decision = LimitDecision(isAllowed = allow, remaining = remainToken.toInt(), retryAfterMs = retry)
        )
    }
}