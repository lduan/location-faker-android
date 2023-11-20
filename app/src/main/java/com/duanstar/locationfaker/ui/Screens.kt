package com.duanstar.locationfaker.ui

import androidx.navigation.NavBackStackEntry
import androidx.navigation.navArgument
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

sealed interface Screens {

    val route: String

    object Main {
        const val route = "main"
    }

    object Search {
        const val route = "search?swLat={swLat},swLng={swLng},neLat={neLat},neLng={neLng}"

        val arguments = listOf(
            navArgument("swLat") { nullable = true },
            navArgument("swLng") { nullable = true },
            navArgument("neLat") { nullable = true },
            navArgument("neLng") { nullable = true },
        )

        fun makeRoute(bounds: LatLngBounds?): String {
            return if (bounds == null) {
                "search"
            } else {
                val sw = bounds.southwest
                val ne = bounds.northeast
                "search?swLat=${sw.latitude},swLng=${sw.longitude},neLat=${ne.latitude},neLng=${ne.longitude}"
            }
        }

        fun getBoundsArgument(entry: NavBackStackEntry): LatLngBounds? {
            val swLat = entry.arguments?.getString("swLat")?.toDoubleOrNull()
            val swLng = entry.arguments?.getString("swLng")?.toDoubleOrNull()
            val neLat = entry.arguments?.getString("neLat")?.toDoubleOrNull()
            val neLng = entry.arguments?.getString("neLng")?.toDoubleOrNull()
            return if (swLat != null && swLng != null && neLat != null && neLng != null) {
                LatLngBounds(LatLng(swLat, swLng), LatLng(neLat, neLng))
            } else {
                null
            }
        }
    }
}