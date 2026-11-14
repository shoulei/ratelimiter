package com.leishou.ratelimiter.model

data class LimitKey(
    // Identifies the rate limit rule, allowing different limits for the same entity type (e.g. normal users vs. VIP users)
    val ruleId: String,
    // The type of entity being rate-limited
    val entityType: EntityType,
    // The specific user ID, IP address, or API key
    val entityId: String
)