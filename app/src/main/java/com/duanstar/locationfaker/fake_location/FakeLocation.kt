package com.duanstar.locationfaker.fake_location

import com.google.android.gms.maps.model.LatLng
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.text.DecimalFormat

@Serializable
data class FakeLocation(
    val latitude: Double,
    val longitude: Double,
    val name: String? = null
) {

    companion object {
        private val FORMATTER = DecimalFormat("#.#####")
    }

    @Transient
    val latLng = LatLng(latitude, longitude)

    @Transient
    val position = "${FORMATTER.format(latitude)}, ${FORMATTER.format(longitude)}"

    @Transient
    val title: String = name ?: position

    @Transient
    val subtitle: String? = if (name != null) position else null
}