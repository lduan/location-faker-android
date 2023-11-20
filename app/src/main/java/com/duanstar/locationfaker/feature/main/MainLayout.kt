package com.duanstar.locationfaker.feature.main

import android.annotation.SuppressLint
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.duanstar.locationfaker.R
import com.duanstar.locationfaker.fake_location.FakeLocation
import com.duanstar.locationfaker.fake_location.FakeLocationStateMachine
import com.duanstar.locationfaker.fake_location.FakeLocationStateMachine.State.ON
import com.duanstar.locationfaker.permission.anyGranted
import com.duanstar.locationfaker.permission.rememberLocationPermission
import com.duanstar.locationfaker.permission.rememberNotificationsPermission
import com.duanstar.locationfaker.ui.theme.Dimensions.marginHorizontal
import com.duanstar.locationfaker.ui.theme.Dimensions.marginVertical
import com.duanstar.locationfaker.ui.theme.Dimensions.padding
import com.duanstar.locationfaker.ui.widgets.CenteredRow
import com.duanstar.locationfaker.ui.widgets.FadeAnimatedVisibility
import com.duanstar.locationfaker.ui.widgets.SingleLineText
import com.duanstar.locationfaker.utils.animateTo
import com.duanstar.locationfaker.utils.bounds
import com.duanstar.locationfaker.utils.latLng
import com.duanstar.locationfaker.utils.moveTo
import com.duanstar.locationfaker.utils.onCameraIdle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import kotlinx.coroutines.launch


@Composable
fun MainLayout(
    viewModel: MainViewModel = hiltViewModel(),
    onSearchClick: (LatLngBounds?) -> Unit
) {
    val fakeLocation by viewModel.fakeLocation.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val mockLocationsEnabled by viewModel.mockLocationsEnabled.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()

    MainLayout(
        cameraPositionState = viewModel.cameraPositionState,
        fakeLocation = fakeLocation,
        favorites = favorites,
        state = state,
        mockLocationsEnabled = mockLocationsEnabled,
        setFakeLocation = viewModel::setFakeLocation,
        setState = viewModel::setState,
        toggleSaved = viewModel::toggleSaved,
        getLastLocation = viewModel::getLastLocation,
        onSearchClick = onSearchClick
    )
}

@SuppressLint("InlinedApi")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainLayout(
    cameraPositionState: CameraPositionState,
    fakeLocation: FakeLocation?,
    favorites: List<FakeLocation>,
    state: FakeLocationStateMachine.State,
    mockLocationsEnabled: Boolean,
    setFakeLocation: (FakeLocation?) -> Unit,
    setState: (Boolean) -> Unit,
    toggleSaved: () -> Unit,
    getLastLocation: suspend () -> Location?,
    onSearchClick: (LatLngBounds?) -> Unit
) {
    val locationPermissionState = rememberLocationPermission()
    LaunchedEffect(Unit) {
        locationPermissionState.launchMultiplePermissionRequest()
    }
    val locationGranted = locationPermissionState.anyGranted

    var notificationPermissionRequested by remember { mutableStateOf(false) }
    val notificationsPermissionState = rememberNotificationsPermission()

    var showLocationPermissionDialog by remember { mutableStateOf(false) }
    var showMockLocationSettingDialog by remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            TopBar(
                showSwitch = fakeLocation != null,
                state = state,
                onSwitchChecked = { checked ->
                    if (!locationGranted) {
                        showLocationPermissionDialog = true
                    } else if (!mockLocationsEnabled) {
                        showMockLocationSettingDialog = true
                    } else if (!notificationsPermissionState.status.isGranted &&
                        (!notificationPermissionRequested || notificationsPermissionState.status.shouldShowRationale)
                    ) {
                        notificationsPermissionState.launchPermissionRequest()
                        notificationPermissionRequested = true
                    } else {
                        setState(checked)
                    }
                }
            )
        }
    ) { p ->
        Box(modifier = Modifier.padding(p)) {
            Map(
                cameraPositionState = cameraPositionState,
                locationGranted = locationGranted,
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
                starred = isFakeLocationSaved,
                onClick = {
                    onSearchClick(cameraPositionState.bounds)
                },
                onCloseClick = {
                    setFakeLocation(null)
                },
                onStarClick = toggleSaved
            )
        }

        LaunchedEffect(locationGranted) {
            // If marker is not already set, move map to user's last location.
            if (locationGranted && fakeLocation == null) {
                getLastLocation()?.latLng?.let { cameraPositionState.moveTo(it) }
            }
        }
    }

    if (showLocationPermissionDialog) {
        LocationPermissionRequiredDialog(
            onDismiss = { showLocationPermissionDialog = false }
        )
    }

    if (showMockLocationSettingDialog) {
        EnableMockLocationSettingDialog(
            onDismiss = { showMockLocationSettingDialog = false }
        )
    }
}

@Composable
private fun TopBar(
    showSwitch: Boolean,
    state: FakeLocationStateMachine.State,
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
        FakeLocationStateMachine.State.OFF -> MaterialTheme.colors.surface
        FakeLocationStateMachine.State.ON -> pulseColor
    }
    val contentColor = when (state) {
        FakeLocationStateMachine.State.OFF -> MaterialTheme.colors.primary
        FakeLocationStateMachine.State.ON -> MaterialTheme.colors.onPrimary
    }
    TopAppBar(
        title = {
            Text(text = stringResource(R.string.app_name))
        },
        actions = {
            FadeAnimatedVisibility(visible = showSwitch) {
                CenteredRow(
                    modifier = Modifier
                        .clickable(
                            onClick = {
                                onSwitchChecked(state != ON)
                            },
                            role = Role.Switch,
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        )
                ) {
                    Text(text = state.name, style = MaterialTheme.typography.button)
                    Switch(
                        checked = state == ON,
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
    starred: Boolean,
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
            if (fakeLocation != null) {
                SingleLineText(
                    text = fakeLocation.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.body2
                )
                if (fakeLocation.name?.isEmpty() == true) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                }
            } else {
                SingleLineText(
                    text = stringResource(R.string.search),
                    modifier = Modifier
                        .weight(1f)
                        .alpha(ContentAlpha.medium),
                    style = MaterialTheme.typography.body2
                )
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
                            if (starred) {
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
    locationGranted: Boolean,
    fakeLocation: FakeLocation?,
    setFakeLocation: (FakeLocation?) -> Unit,
    getLastLocation: suspend () -> Location?
) {
    val properties = remember(locationGranted) {
        MapProperties(isMyLocationEnabled = locationGranted)
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
                    FadeAnimatedVisibility(visible = locationGranted) {
                        MyLocationButton(onClick = {
                            coroutineScope.launch {
                                getLastLocation()?.latLng?.let { cameraPositionState.animateTo(it) }
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
