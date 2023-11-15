package com.duanstar.locationfaker.feature.main

import android.location.Geocoder
import android.location.Location
import com.duanstar.locationfaker.BaseViewModel
import com.duanstar.locationfaker.fake_location.FakeLocation
import com.duanstar.locationfaker.fake_location.FakeLocationStateMachine
import com.duanstar.locationfaker.fake_location.FakeLocationStream
import com.duanstar.locationfaker.launch
import com.duanstar.locationfaker.location.awaitAddress
import com.duanstar.locationfaker.location.awaitLastLocation
import com.duanstar.locationfaker.location.toShortAddress
import com.duanstar.locationfaker.permission.MockLocationSetting
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val geocoder: Geocoder,
    private val locationClient: FusedLocationProviderClient,
    private val mockLocationSetting: MockLocationSetting,
    private val stream: FakeLocationStream,
    private val stateMachine: FakeLocationStateMachine
) : BaseViewModel() {

    val fakeLocation = stream.fakeLocation
    val isGeocoding = MutableStateFlow(false)
    val mockLocationsEnabled = mockLocationSetting.enabled
    val state = stateMachine.state

    suspend fun getLastLocation(): Location? {
        return try {
            locationClient.awaitLastLocation()
        } catch (e: SecurityException) {
            Timber.e("Location permission not granted.")
            null
        }
    }

    fun setFakeLocation(fakeLocation: FakeLocation?) {
        // Try and look-up the address for fake location first
        if (fakeLocation != null && fakeLocation.name.isNullOrBlank()) {
            // Show a blank name while we geocode
            stream.update(fakeLocation.copy(name = ""))

            launch {
                isGeocoding.value = true

                val address = geocoder.awaitAddress(latitude = fakeLocation.latitude, longitude = fakeLocation.longitude)
                    ?.toShortAddress()
                stream.update(fakeLocation.copy(name = address))

                isGeocoding.value = false
            }
        } else {
            stream.update(fakeLocation)
        }
    }

    fun setState(on: Boolean) {
        if (on) {
            stateMachine.on()
        } else {
            stateMachine.off()
        }
    }
}

