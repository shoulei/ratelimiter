package com.leishou.ratelimiter.model

data class LimitRule(
    val id: String,
    val entityType: EntityType,
    val config: LimitAlgorithmConfig,
    val enabled: Boolean = true
)