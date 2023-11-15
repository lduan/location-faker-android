package com.duanstar.locationfaker.location

import android.location.Address
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException

suspend fun Geocoder.awaitAddress(latitude: Double, longitude: Double): Address? {
    return withContext(Dispatchers.IO) {
        try {
            getFromLocation(latitude, longitude, 1)?.firstOrNull()
        } catch (e: NoSuchElementException) {
            // No address result
            Timber.w(e, "No address at location = ($latitude, $longitude)")
            null
        } catch (e: IOException) {
            // No internet
            Timber.w(e)
            null
        }
    }
}

fun Address.toShortAddress(): String? {
    return getAddressLine(0)?.substringBefore(",")
}