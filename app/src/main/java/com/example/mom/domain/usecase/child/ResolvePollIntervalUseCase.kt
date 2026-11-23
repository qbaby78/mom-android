package com.example.mom.domain.usecase.child

import com.example.mom.domain.model.Interest
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Poll interval for one child is the shortest interest interval.
 * An empty interest list means the caller should not start a location poll.
 */
class ResolvePollIntervalUseCase {
    operator fun invoke(interests: List<Interest>): Duration? {
        if (interests.isEmpty()) return null
        return interests.minOf { it.intervalMinutes }.minutes
    }
}
