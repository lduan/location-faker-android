package com.duanstar.locationfaker.feature.main

import android.location.Geocoder
import android.location.Location
import com.duanstar.locationfaker.BaseViewModel
import com.duanstar.locationfaker.fake_location.FakeLocation
import com.duanstar.locationfaker.fake_location.FakeLocationStateMachine
import com.duanstar.locationfaker.fake_location.FakeLocationStream
import com.duanstar.locationfaker.feature.favorites.FavoritesManager
import com.duanstar.locationfaker.launch
import com.duanstar.locationfaker.permission.isNotGranted
import com.duanstar.locationfaker.settings.MockLocationSettingMonitor
import com.duanstar.locationfaker.utils.awaitAddress
import com.duanstar.locationfaker.utils.awaitLastLocation
import com.duanstar.locationfaker.utils.toShortAddress
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalPermissionsApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    private val favoritesManager: FavoritesManager,
    private val geocoder: Geocoder,
    private val locationClient: FusedLocationProviderClient,
    private val mockLocationSettingMonitor: MockLocationSettingMonitor,
    private val stream: FakeLocationStream,
    private val stateMachine: FakeLocationStateMachine
) : BaseViewModel() {

    val fakeLocation = stream.fakeLocation
    val locationPermissionStatus = MutableStateFlow<PermissionStatus?>(null)
    val notificationPermissionStatus = MutableStateFlow<PermissionStatus?>(null)
    val state = stateMachine.state

    private val favorites = favoritesManager.favorites

    val isFakeLocationSaved = combine(favorites, fakeLocation) { favorites, fakeLocation ->
        favorites.contains(fakeLocation)
    }

    val nextStep = combine(
        locationPermissionStatus,
        notificationPermissionStatus,
        mockLocationSettingMonitor.enabled
    ) { locationPermissionStatus, notificationPermissionStatus, mockLocationSettingEnabled ->
        when {
            locationPermissionStatus == null -> {
                NextStep.LocationPermissionNeeded(true)
            }
            locationPermissionStatus.isNotGranted -> {
                NextStep.LocationPermissionNeeded(locationPermissionStatus.shouldShowRationale)
            }
            notificationPermissionStatus == null -> {
                NextStep.NotificationPermissionNeeded(true)
            }
            notificationPermissionStatus.isNotGranted -> {
                NextStep.NotificationPermissionNeeded(notificationPermissionStatus.shouldShowRationale)
            }
            !mockLocationSettingEnabled -> {
                NextStep.MockLocationSettingNeeded
            }
            else -> NextStep.Ready
        }
    }


    suspend fun getLastLocation(): Location? {
        return try {
            locationClient.awaitLastLocation()
        } catch (e: SecurityException) {
            Timber.e(e, "Permission not granted.")
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

    fun toggleSave() {
        fakeLocation.value?.let {
            if (favorites.value.contains(it)) {
                favoritesManager.remove(it)
            } else {
                favoritesManager.add(it)
            }
        }
    }

    fun turnOn() = stateMachine.on()

    fun turnOff() = stateMachine.off()

    sealed class NextStep {

        data class LocationPermissionNeeded(val shouldRequest: Boolean) : NextStep()

        data class NotificationPermissionNeeded(val shouldRequest: Boolean) : NextStep()

        data object MockLocationSettingNeeded : NextStep()

        data object Ready : NextStep()
    }
}

