@file:OptIn(ExperimentalPermissionsApi::class)

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
    val state = stateMachine.state
    val locationPermissionStatus = MutableStateFlow<PermissionStatus?>(null)
    val notificationPermissionStatus = MutableStateFlow<PermissionStatus?>(null)

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

    fun isFakeLocationSaved(): Flow<Boolean> {
        return combine(fakeLocation, favoritesManager.favorites) { fakeLocation, favorites ->
            favorites.contains(fakeLocation)
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

    fun toggleSave() {
        fakeLocation.value?.let {
            favoritesManager.addOrRemove(it)
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

    sealed class NextStep {

        data class LocationPermissionNeeded(val shouldRequest: Boolean) : NextStep()

        data class NotificationPermissionNeeded(val shouldRequest: Boolean) : NextStep()

        object MockLocationSettingNeeded : NextStep()

        object Ready : NextStep()
    }
}

