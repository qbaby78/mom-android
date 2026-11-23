package com.example.mom.core.util

import com.example.mom.domain.model.GeoPoint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GeofenceMathTest {
    @Test
    fun `given same coordinates when distanceKm then returns 0 km`() {
        val point = GeoPoint(latitude = 37.5665, longitude = 126.9780)

        val distanceKm = GeofenceMath.distanceKm(point, point)

        assertEquals(0.0, distanceKm, 0.0)
    }

    @Test
    fun `given one degree of latitude when distanceKm then returns about 111 km`() {
        val from = GeoPoint(latitude = 0.0, longitude = 0.0)
        val to = GeoPoint(latitude = 1.0, longitude = 0.0)

        val distanceKm = GeofenceMath.distanceKm(from, to)

        assertEquals(111.0, distanceKm, 1.0)
    }

    @Test
    fun `given equal longitude offsets near the equator when distanceKm then east and west distances match`() {
        val origin = GeoPoint(latitude = 0.0, longitude = 0.0)
        val east = GeoPoint(latitude = 0.0, longitude = 1.0)
        val west = GeoPoint(latitude = 0.0, longitude = -1.0)

        val eastKm = GeofenceMath.distanceKm(origin, east)
        val westKm = GeofenceMath.distanceKm(origin, west)

        assertEquals(eastKm, westKm, 0.000_001)
    }
}
