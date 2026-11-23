package com.example.mom.domain.model

data class ActiveViolation(
    val interestId: Long,
    val childId: Long,
    val startedAt: Long,
)
