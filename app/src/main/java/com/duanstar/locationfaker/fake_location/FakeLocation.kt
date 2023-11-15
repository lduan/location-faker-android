package com.duanstar.locationfaker.fake_location

import com.google.android.gms.maps.model.LatLng
import java.text.DecimalFormat

data class FakeLocation(
    val latitude: Double,
    val longitude: Double,
    val name: String? = null
) {

    companion object {
        private val FORMATTER = DecimalFormat("#.#####")
    }

    val latLng = LatLng(latitude, longitude)

    val title: String = name ?: "${FORMATTER.format(latitude)}, ${FORMATTER.format(longitude)}"
}

fun LatLng.toFakeLocation() = FakeLocation(latitude = latitude, longitude = longitude)