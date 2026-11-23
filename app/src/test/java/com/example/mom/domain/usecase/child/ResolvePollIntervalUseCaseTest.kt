package com.example.mom.domain.usecase.child

import com.example.mom.domain.model.Interest
import com.example.mom.domain.model.InterestType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes

class ResolvePollIntervalUseCaseTest {
    private val useCase = ResolvePollIntervalUseCase()

    @Test
    fun `given several interests when resolve poll interval then returns the shortest interval`() {
        val interests = listOf(
            interest(id = 1L, intervalMinutes = 30),
            interest(id = 2L, intervalMinutes = 5),
            interest(id = 3L, intervalMinutes = 15),
        )

        val interval = useCase(interests)

        assertEquals(5.minutes, interval)
    }

    @Test
    fun `given no interests when resolve poll interval then returns null`() {
        val interval = useCase(emptyList())

        assertNull(interval)
    }

    private fun interest(id: Long, intervalMinutes: Int) = Interest(
        id = id,
        childId = 1L,
        name = "school",
        type = InterestType.GEOFENCE,
        latitude = 37.5,
        longitude = 127.0,
        radiusKm = 1.0,
        intervalMinutes = intervalMinutes,
    )
}
