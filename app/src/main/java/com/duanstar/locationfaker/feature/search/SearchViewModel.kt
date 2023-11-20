package com.duanstar.locationfaker.feature.search

import com.duanstar.locationfaker.BaseViewModel
import com.duanstar.locationfaker.fake_location.FakeLocation
import com.duanstar.locationfaker.fake_location.FakeLocationStream
import com.duanstar.locationfaker.feature.favorites.FavoritesManager
import com.duanstar.locationfaker.utils.bounds
import com.duanstar.locationfaker.utils.moveTo
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.ktx.api.net.awaitFetchPlace
import com.google.android.libraries.places.ktx.api.net.awaitFindAutocompletePredictions
import com.google.maps.android.compose.CameraPositionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val cameraPositionState: CameraPositionState,
    private val fakeLocationStream: FakeLocationStream,
    private val favoritesManager: FavoritesManager,
    private val placesClient: PlacesClient
) : BaseViewModel() {

    companion object {
        private const val DEBOUNCE_MS = 300L
    }

    private val queryStateFlow = MutableStateFlow<String?>(null)

    private var locationBias: LatLngBounds? = null
    private var sessionToken: AutocompleteSessionToken? = null

    val autocompletePredictions: StateFlow<List<AutocompletePrediction>> = queryStateFlow.mapLatest { query ->
        if (query.isNullOrBlank()) {
            // If query is blank, immediately return no results.
            emptyList()
        } else {
            delay(DEBOUNCE_MS)

            // If no session token, generate one.
            if (sessionToken == null) {
                sessionToken = AutocompleteSessionToken.newInstance()
            }
            status.value = ApiStatus.OK
            try {
                placesClient.awaitFindAutocompletePredictions {
                    Timber.e("bias=${cameraPositionState.bounds}")
                    this@SearchViewModel.locationBias?.let {
                        locationBias = RectangularBounds.newInstance(it)
                    }
                    sessionToken = this@SearchViewModel.sessionToken
                    setQuery(query)
                }.autocompletePredictions
            } catch (e: ApiException) {
                Timber.e(e, "Failed to fetch autocomplete prediction.")
                status.value = ApiStatus.Error(e.status.statusMessage)
                null
            }
        }
    }.filterNotNull().stateIn(emptyList())

    val favorites = combine(queryStateFlow, favoritesManager.favorites) { query, favorites ->
        if (query.isNullOrBlank()) {
            favorites
        } else {
            favorites.filter { favorite ->
                favorite.name?.contains(query, ignoreCase = true) == true
            }
        }
    }.stateIn(favoritesManager.favorites.value)

    val status = MutableStateFlow<ApiStatus>(ApiStatus.OK)

    fun removeFavorite(favorite: FakeLocation) {
        favoritesManager.remove(favorite)
    }

    fun setFakeLocation(fakeLocation: FakeLocation) {
        fakeLocationStream.update(fakeLocation)
        cameraPositionState.moveTo(fakeLocation.latLng)
    }

    suspend fun setFakeLocation(prediction: AutocompletePrediction): Boolean {
        status.value = ApiStatus.OK
        val place = try {
            placesClient.awaitFetchPlace(
                placeId = prediction.placeId,
                placeFields = listOf(Place.Field.NAME, Place.Field.LAT_LNG)
            ).place
        } catch (e: ApiException) {
            status.value = ApiStatus.Error(e.status.statusMessage)
            Timber.e(e, "Failed to fetch place for prediction=$prediction.")
            return false
        }

        // LatLng is non-null because we requested the field above.
        val fakeLocation = FakeLocation(
            latitude = place.latLng!!.latitude,
            longitude = place.latLng!!.longitude,
            name = place.name
        )
        fakeLocationStream.update(fakeLocation)
        cameraPositionState.moveTo(fakeLocation.latLng)
        sessionToken = null
        return true
    }

    fun setLocationBias(bounds: LatLngBounds?) {
        locationBias = bounds
    }

    fun setQuery(query: String) {
        queryStateFlow.value = query
    }
}

sealed interface ApiStatus {

    object OK : ApiStatus

    data class Error(val message: String?) : ApiStatus
}