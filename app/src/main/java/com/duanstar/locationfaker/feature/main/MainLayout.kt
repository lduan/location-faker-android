package com.duanstar.locationfaker.feature.main

import android.location.Location
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ContentAlpha.disabled
import androidx.compose.material.ContentAlpha.high
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.duanstar.locationfaker.R
import com.duanstar.locationfaker.fake_location.FakeLocation
import com.duanstar.locationfaker.fake_location.FakeLocationStateMachine.State
import com.duanstar.locationfaker.feature.main.dialogs.EnableMockLocationSettingDialog
import com.duanstar.locationfaker.feature.main.dialogs.LocationPermissionRequiredDialog
import com.duanstar.locationfaker.location.toLatLng
import com.duanstar.locationfaker.permission.anyGranted
import com.duanstar.locationfaker.permission.requireLocationPermission
import com.duanstar.locationfaker.ui.FadeAnimatedVisibility
import com.duanstar.locationfaker.ui.theme.Dimensions.marginHorizontal
import com.duanstar.locationfaker.ui.theme.Dimensions.marginVertical
import com.duanstar.locationfaker.ui.theme.Dimensions.padding
import com.duanstar.locationfaker.ui.widgets.CenteredRow
import com.duanstar.locationfaker.ui.widgets.SingleLineText
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import timber.log.Timber

private val SAN_FRANCISCO_LAT_LNG = LatLng(37.7749, -122.4194)

@Composable
fun MainLayout(
    viewModel: MainViewModel = hiltViewModel(),
    onSearchClick: (LatLng) -> Unit
) {
    val fakeLocation by viewModel.fakeLocation.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val isGeocoding by viewModel.isGeocoding.collectAsStateWithLifecycle()
    val mockLocationsEnabled by viewModel.mockLocationsEnabled.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()

    MainLayout(
        fakeLocation = fakeLocation,
        favorites = favorites,
        isGeocoding = isGeocoding,
        state = state,
        mockLocationsEnabled = mockLocationsEnabled,
        setFakeLocation = viewModel::setFakeLocation,
        setState = viewModel::setState,
        toggleSaved = viewModel::toggleSaved,
        getLastLocation = viewModel::getLastLocation,
        onSearchClick = onSearchClick
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainLayout(
    fakeLocation: FakeLocation?,
    favorites: List<FakeLocation>,
    isGeocoding: Boolean,
    state: State,
    mockLocationsEnabled: Boolean,
    setFakeLocation: (FakeLocation?) -> Unit,
    setState: (Boolean) -> Unit,
    toggleSaved: () -> Unit,
    getLastLocation: suspend () -> Location?,
    onSearchClick: (LatLng) -> Unit
) {
    val permissionState = requireLocationPermission()
    val permissionGranted = permissionState.anyGranted

    var showPermissionDialog by remember { mutableStateOf(false) }
    var showMockLocationSettingDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppBar(
                showSwitch = fakeLocation != null,
                state = state,
                onSwitchChecked = { checked ->
                    if (!permissionGranted) {
                        showPermissionDialog = true
                    } else if (!mockLocationsEnabled) {
                        showMockLocationSettingDialog = true
                    } else {
                        setState(checked)
                    }
                }
            )
        }
    ) { p ->
        Box(modifier = Modifier.padding(p)) {
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(fakeLocation?.latLng ?: SAN_FRANCISCO_LAT_LNG, 15f)
            }

            Map(
                cameraPositionState = cameraPositionState,
                permissionGranted = permissionGranted,
                fakeLocation = fakeLocation,
                setFakeLocation = setFakeLocation,
                getLastLocation = getLastLocation
            )

            val isFakeLocationSaved by remember(fakeLocation, favorites) {
                derivedStateOf {
                    favorites.contains(fakeLocation)
                }
            }
            SearchBar(
                fakeLocation = fakeLocation,
                geocoding = isGeocoding,
                isStarred = isFakeLocationSaved,
                onClick = {
                    onSearchClick(cameraPositionState.position.target)
                },
                onCloseClick = {
                    setFakeLocation(null)
                },
                onStarClick = toggleSaved
            )
        }
    }

    if (showPermissionDialog) {
        LocationPermissionRequiredDialog(
            permissionState = permissionState,
            onDismiss = { showPermissionDialog = false }
        )
    }

    if (showMockLocationSettingDialog) {
        EnableMockLocationSettingDialog(
            onDismiss = { showMockLocationSettingDialog = false }
        )
    }
}

@Composable
private fun AppBar(
    showSwitch: Boolean,
    state: State,
    onSwitchChecked: (Boolean) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseColor by infiniteTransition.animateColor(
        initialValue = MaterialTheme.colors.primary,
        targetValue = MaterialTheme.colors.primaryVariant,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val backgroundColor = when (state) {
        State.OFF -> MaterialTheme.colors.surface
        State.ON -> pulseColor
    }
    val contentColor = when (state) {
        State.OFF -> MaterialTheme.colors.primary
        State.ON -> MaterialTheme.colors.onPrimary
    }
    TopAppBar(
        title = {
            Text(text = stringResource(R.string.app_name))
        },
        actions = {
            FadeAnimatedVisibility(visible = showSwitch) {
                CenteredRow {
                    Text(
                        text = state.name,
                        style = MaterialTheme.typography.button,
                        modifier = Modifier
                            .clickable(
                                onClick = {
                                    onSwitchChecked(state != State.ON)
                                },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            )
                            .padding(vertical = 8.dp)
                    )
                    Switch(
                        checked = state == State.ON,
                        onCheckedChange = onSwitchChecked
                    )
                }
            }
        },
        backgroundColor = backgroundColor,
        contentColor = contentColor
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SearchBar(
    fakeLocation: FakeLocation?,
    geocoding: Boolean,
    isStarred: Boolean,
    onClick: () -> Unit,
    onCloseClick: () -> Unit,
    onStarClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.padding(padding),
        shape = CircleShape
    ) {
        CenteredRow(
            modifier = Modifier
                .height(48.dp)
                .padding(start = 4.dp, end = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .alpha(ContentAlpha.medium)
            )
            SingleLineText(
                text = fakeLocation?.title ?: stringResource(R.string.search),
                modifier = Modifier
                    .weight(1f)
                    .alpha(fakeLocation?.let { high } ?: disabled),
                style = MaterialTheme.typography.body2
            )
            if (geocoding) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            }
            FadeAnimatedVisibility(visible = fakeLocation != null) {
                CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                    Row {
                        IconButton(
                            onClick = onCloseClick,
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.clear_fake_location),
                                modifier = Modifier.alpha(ContentAlpha.medium)
                            )
                        }
                        IconButton(
                            onClick = onStarClick,
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 8.dp)
                        ) {
                            if (isStarred) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = stringResource(R.string.added_to_favorites),
                                    tint = MaterialTheme.colors.primary,
                                    modifier = Modifier.alpha(ContentAlpha.high)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.StarOutline,
                                    contentDescription = stringResource(R.string.add_to_favorites),
                                    modifier = Modifier.alpha(ContentAlpha.medium)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Map(
    cameraPositionState: CameraPositionState,
    permissionGranted: Boolean,
    fakeLocation: FakeLocation?,
    setFakeLocation: (FakeLocation?) -> Unit,
    getLastLocation: suspend () -> Location?
) {
    val properties = remember(permissionGranted) {
        MapProperties(isMyLocationEnabled = permissionGranted)
    }
    val uiSettings = remember {
        MapUiSettings(
            mapToolbarEnabled = false,
            tiltGesturesEnabled = false,
            zoomControlsEnabled = false
        )
    }

    var cameraIdleBounds by remember { mutableStateOf(cameraPositionState.bounds) }
    val isMarkerOffscreen by remember(cameraIdleBounds, fakeLocation) {
        derivedStateOf {
            fakeLocation?.let { cameraIdleBounds?.contains(fakeLocation.latLng) } == false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            cameraPositionState = cameraPositionState,
            properties = properties,
            uiSettings = uiSettings,
            onMapLongClick = {
                setFakeLocation(FakeLocation(latitude = it.latitude, longitude = it.longitude))
            },
            contentPadding = PaddingValues(top = 72.dp),
            myLocationButton = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 80.dp, bottom = marginVertical, end = marginHorizontal),
                    horizontalAlignment = Alignment.End
                ) {
                    val coroutineScope = rememberCoroutineScope()
                    FadeAnimatedVisibility(visible = isMarkerOffscreen) {
                        FindMarkerButton(onClick = {
                            coroutineScope.launch {
                                fakeLocation?.latLng?.let { cameraPositionState.animateTo(it) }
                            }
                        })
                    }
                    Spacer(Modifier.weight(1f))
                    FadeAnimatedVisibility(visible = permissionGranted) {
                        MyLocationButton(onClick = {
                            coroutineScope.launch {
                                getLastLocation()?.toLatLng()?.let { cameraPositionState.animateTo(it) }
                            }
                        })
                    }
                }
            }
        ) {
            fakeLocation?.let {
                Marker(state = MarkerState(fakeLocation.latLng))
            }

            cameraPositionState.onCameraIdle {
                cameraIdleBounds = cameraPositionState.bounds
            }
            LaunchedEffect(permissionGranted) {
                // If no pin is already set, set the initial map position to current user location.
                if (permissionGranted && fakeLocation == null) {
                    getLastLocation()?.toLatLng()?.let {
                        Timber.e("Last location found, moving to $it")
                        cameraPositionState.moveTo(it)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MyLocationButton(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.alpha(.9f)
    ) {
        Icon(
            imageVector = Icons.Filled.NearMe,
            contentDescription = stringResource(R.string.my_location),
            modifier = Modifier
                .alpha(.75f)
                .padding(8.dp)
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun FindMarkerButton(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.alpha(.9f),
    ) {
        Icon(
            imageVector = Icons.Filled.Place,
            contentDescription = stringResource(R.string.marker_location),
            modifier = Modifier
                .alpha(.75f)
                .padding(8.dp)
        )
    }
}

private val CameraPositionState.bounds
    get() = projection?.visibleRegion?.latLngBounds

@Composable
private fun CameraPositionState.onCameraIdle(onIdle: () -> Unit) {
    LaunchedEffect(isMoving) {
        if (!isMoving) {
            onIdle()
            Timber.e("Camera moved to ${position.target}")
        }
    }
}

private suspend fun CameraPositionState.animateTo(newLatLng: LatLng, minZoom: Float = 13f, maxZoom: Float = 17f) {
    val zoom = position.zoom.coerceIn(minZoom, maxZoom)
    animate(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(newLatLng, zoom)))
}

private fun CameraPositionState.moveTo(newLatLng: LatLng, minZoom: Float = 13f, maxZoom: Float = 17f) {
    val zoom = position.zoom.coerceIn(minZoom, maxZoom)
    move(CameraUpdateFactory.newLatLngZoom(newLatLng, zoom))
}