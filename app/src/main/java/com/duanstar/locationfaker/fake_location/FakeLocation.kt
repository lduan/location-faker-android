package com.duanstar.locationfaker.fake_location

import com.google.android.gms.maps.model.LatLng
import com.squareup.moshi.JsonClass
import java.text.DecimalFormat

@JsonClass(generateAdapter = true)
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