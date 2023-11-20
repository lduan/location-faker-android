package com.duanstar.locationfaker.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState

val CameraPositionState.bounds
    get() = projection?.visibleRegion?.latLngBounds

suspend fun CameraPositionState.animateTo(newLatLng: LatLng, minZoom: Float = 13f, maxZoom: Float = 17f) {
    val zoom = position.zoom.coerceIn(minZoom, maxZoom)
    animate(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(newLatLng, zoom)))
}

fun CameraPositionState.moveTo(newLatLng: LatLng, minZoom: Float = 13f, maxZoom: Float = 17f) {
    val zoom = position.zoom.coerceIn(minZoom, maxZoom)
    move(CameraUpdateFactory.newLatLngZoom(newLatLng, zoom))
}

@Composable
fun CameraPositionState.onCameraIdle(onIdle: () -> Unit) {
    LaunchedEffect(isMoving) {
        if (!isMoving) {
            onIdle()
        }
    }
}

