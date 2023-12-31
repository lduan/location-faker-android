package com.duanstar.locationfaker.feature.main

import android.location.Geocoder
import android.location.Location
import com.duanstar.locationfaker.BaseViewModel
import com.duanstar.locationfaker.fake_location.FakeLocation
import com.duanstar.locationfaker.fake_location.FakeLocationStateMachine
import com.duanstar.locationfaker.fake_location.FakeLocationStream
import com.duanstar.locationfaker.feature.favorites.FavoritesManager
import com.duanstar.locationfaker.launch
import com.duanstar.locationfaker.settings.MockLocationSetting
import com.duanstar.locationfaker.utils.awaitAddress
import com.duanstar.locationfaker.utils.awaitLastLocation
import com.duanstar.locationfaker.utils.toShortAddress
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.maps.android.compose.CameraPositionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val cameraPositionState: CameraPositionState,
    private val favoritesManager: FavoritesManager,
    private val geocoder: Geocoder,
    private val locationClient: FusedLocationProviderClient,
    private val mockLocationSetting: MockLocationSetting,
    private val stream: FakeLocationStream,
    private val stateMachine: FakeLocationStateMachine
) : BaseViewModel() {

    val fakeLocation: StateFlow<FakeLocation?> = stream.fakeLocation
    val favorites: StateFlow<List<FakeLocation>> = favoritesManager.favorites
    val mockLocationsEnabled: StateFlow<Boolean> = mockLocationSetting.enabled
    val state: StateFlow<FakeLocationStateMachine.State> = stateMachine.state

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
                val latitude = fakeLocation.latitude
                val longitude = fakeLocation.longitude
                val address = geocoder.awaitAddress(latitude = latitude, longitude = longitude)?.toShortAddress()
                stream.update(fakeLocation.copy(name = address))
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

    fun toggleSaved() {
        fakeLocation.value?.let {
            favoritesManager.addOrRemove(it)
        }
    }
}

