package com.duanstar.locationfaker.location

import android.Manifest
import android.location.Location
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
suspend fun FusedLocationProviderClient.awaitLastLocation(): Location {

    return suspendCancellableCoroutine { continuation ->
        lastLocation
            .addOnSuccessListener { location ->
                continuation.resume(location)
            }
            .addOnFailureListener { e ->
                Timber.e(e, "FusedLocationProviderClient.getLastLocation failed.")
                continuation.resumeWithException(e)
            }
    }
}

@RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
suspend fun FusedLocationProviderClient.awaitCurrentLocation(cancellationSource: CancellationTokenSource): Location {
    return suspendCancellableCoroutine { continuation ->
        getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancellationSource.token)
            .addOnSuccessListener { location ->
                continuation.resume(location)
            }
            .addOnFailureListener { e ->
                Timber.e(e, "FusedLocationProviderClient.getCurrentLocation failed.")
                continuation.resumeWithException(e)
            }
        continuation.invokeOnCancellation {
            cancellationSource.cancel()
        }
    }
}

@RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
fun FusedLocationProviderClient.mockLocation(location: Location) {
    setMockMode(true)
    setMockLocation(location)
}

@RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
fun FusedLocationProviderClient.stopMockLocation() {
    setMockMode(false)
}

fun Location.toLatLng() = LatLng(latitude, longitude)