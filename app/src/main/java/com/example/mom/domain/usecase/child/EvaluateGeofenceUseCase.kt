package com.example.mom.domain.usecase.child

import com.example.mom.core.util.GeofenceMath
import com.example.mom.domain.model.GeoPoint
import com.example.mom.domain.model.GeofenceCheckResult

/**
 * Compares a measured point with a geofence center.
 * A point on the radius boundary is inside the fence.
 */
class EvaluateGeofenceUseCase {
    operator fun invoke(
        interestId: Long,
        center: GeoPoint,
        radiusKm: Double,
        measured: GeoPoint,
    ): GeofenceCheckResult {
        val distanceKm = GeofenceMath.distanceKm(center, measured)
        return GeofenceCheckResult(
            interestId = interestId,
            distanceKm = distanceKm,
            violated = distanceKm > radiusKm,
        )
    }
}
