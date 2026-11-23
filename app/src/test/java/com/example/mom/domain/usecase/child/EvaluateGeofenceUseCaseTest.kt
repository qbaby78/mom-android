package com.example.mom.domain.usecase.child

import com.example.mom.core.util.GeofenceMath
import com.example.mom.domain.model.GeoPoint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EvaluateGeofenceUseCaseTest {
    private val useCase = EvaluateGeofenceUseCase()
    private val interestId = 7L
    private val center = GeoPoint(latitude = 37.5665, longitude = 126.9780)
    private val measured = GeoPoint(latitude = 37.5755, longitude = 126.9780)

    @Test
    fun `given measured point inside radius when evaluate then violated is false`() {
        val distanceKm = GeofenceMath.distanceKm(center, measured)

        val result = useCase(
            interestId = interestId,
            center = center,
            radiusKm = distanceKm + 1.0,
            measured = measured,
        )

        assertEquals(interestId, result.interestId)
        assertEquals(distanceKm, result.distanceKm, 0.0)
        assertFalse(result.violated)
    }

    @Test
    fun `given distance equal to radius when evaluate then violated is false`() {
        val radiusKm = GeofenceMath.distanceKm(center, measured)

        val result = useCase(
            interestId = interestId,
            center = center,
            radiusKm = radiusKm,
            measured = measured,
        )

        assertEquals(radiusKm, result.distanceKm, 0.0)
        assertFalse(result.violated)
    }

    @Test
    fun `given distance greater than radius when evaluate then violated is true`() {
        val distanceKm = GeofenceMath.distanceKm(center, measured)

        val result = useCase(
            interestId = interestId,
            center = center,
            radiusKm = distanceKm - 0.01,
            measured = measured,
        )

        assertTrue(result.distanceKm > distanceKm - 0.01)
        assertTrue(result.violated)
        assertEquals(distanceKm, result.distanceKm, 0.0)
    }
}
