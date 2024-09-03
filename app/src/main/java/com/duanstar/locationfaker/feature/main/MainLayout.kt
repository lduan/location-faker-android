@file:OptIn(ExperimentalPermissionsApi::class)

package com.duanstar.locationfaker.feature.main

import android.content.res.Configuration
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.duanstar.locationfaker.R
import com.duanstar.locationfaker.fake_location.FakeLocation
import com.duanstar.locationfaker.fake_location.FakeLocationStateMachine
import com.duanstar.locationfaker.fake_location.FakeLocationStateMachine.State.OFF
import com.duanstar.locationfaker.fake_location.FakeLocationStateMachine.State.ON
import com.duanstar.locationfaker.feature.main.MainViewModel.NextStep
import com.duanstar.locationfaker.permission.rememberLocationPermission
import com.duanstar.locationfaker.permission.rememberNotificationsPermission
import com.duanstar.locationfaker.ui.theme.AppTheme
import com.duanstar.locationfaker.ui.theme.Dimensions.cardElevation
import com.duanstar.locationfaker.ui.theme.Dimensions.padding
import com.duanstar.locationfaker.ui.theme.primaryOnSurface
import com.duanstar.locationfaker.ui.widgets.CenteredRow
import com.duanstar.locationfaker.ui.widgets.FadeAnimatedVisibility
import com.duanstar.locationfaker.ui.widgets.SingleLineText
import com.duanstar.locationfaker.utils.animateTo
import com.duanstar.locationfaker.utils.bounds
import com.duanstar.locationfaker.utils.latLng
import com.duanstar.locationfaker.utils.moveTo
import com.duanstar.locationfaker.utils.onCameraIdle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

private val SAN_FRANCISCO_LAT_LNG = LatLng(37.7749, -122.4194)

@Composable
fun MainLayout(
    viewModel: MainViewModel = hiltViewModel(),
    onSearchClick: (LatLngBounds?) -> Unit
) {
    val fakeLocation by viewModel.fakeLocation.collectAsStateWithLifecycle()
    val isFakeLocationSaved by viewModel.isFakeLocationSaved().collectAsStateWithLifecycle(false)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val nextStep by viewModel.nextStep.collectAsStateWithLifecycle(NextStep.Ready)

    MainLayout(
        fakeLocation = fakeLocation,
        isFakeLocationSaved = isFakeLocationSaved,
        state = state,
        nextStep = nextStep,
        setLocationPermissionStatus = viewModel.locationPermissionStatus::value::set,
        setNotificationPermissionStatus = viewModel.notificationPermissionStatus::value::set,
        setFakeLocation = viewModel::setFakeLocation,
        setState = viewModel::setState,
        toggleSave = viewModel::toggleSave,
        getLastLocation = viewModel::getLastLocation,
        onSearchClick = onSearchClick
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainLayout(
    fakeLocation: FakeLocation?,
    isFakeLocationSaved: Boolean,
    state: FakeLocationStateMachine.State,
    nextStep: NextStep,
    setLocationPermissionStatus: (PermissionStatus) -> Unit,
    setNotificationPermissionStatus: (PermissionStatus) -> Unit,
    setFakeLocation: (FakeLocation?) -> Unit,
    setState: (Boolean) -> Unit,
    toggleSave: () -> Unit,
    getLastLocation: suspend () -> Location?,
    onSearchClick: (LatLngBounds?) -> Unit
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(fakeLocation?.latLng ?: SAN_FRANCISCO_LAT_LNG, 15f)
    }

    // Location permission setup
    var requestedLocationPermission by rememberSaveable { mutableStateOf(false) }
    val locationPermission = rememberLocationPermission {
        requestedLocationPermission = true
    }
    LaunchedEffect(requestedLocationPermission, locationPermission.status) {
        if (requestedLocationPermission) {
            setLocationPermissionStatus(locationPermission.status)
        }
    }
    // Move map to user's last location, if no fake location is set.
    LaunchedEffect(locationPermission.status.isGranted) {
        if (locationPermission.status.isGranted && fakeLocation == null) {
            getLastLocation()?.latLng?.let(cameraPositionState::moveTo)
        }
    }
    // Request location permission on launch
    LaunchedEffect(Unit) {
        locationPermission.launchPermissionRequest()
    }

    // Notification permission setup
    var requestedNotificationPermission by rememberSaveable { mutableStateOf(false) }
    val notificationPermission = rememberNotificationsPermission {
        requestedNotificationPermission = true
    }
    LaunchedEffect(requestedNotificationPermission, notificationPermission.status) {
        if (requestedNotificationPermission) {
            setNotificationPermissionStatus(notificationPermission.status)
        }
    }

    var showLocationPermissionNeededDialog by remember { mutableStateOf(false) }
    var showNotificationPermissionNeededDialog by remember { mutableStateOf(false) }
    var showMockLocationSettingDialog by remember { mutableStateOf(false) }

    // Store try-start as a counter, so we can increment it and continue the flow from the next required step
    var tryStart by remember { mutableIntStateOf(0) }
    LaunchedEffect(tryStart, nextStep::class) {
        if (tryStart > 0) {
            when (nextStep) {
                is NextStep.LocationPermissionNeeded -> {
                    if (nextStep.shouldRequest) {
                        locationPermission.launchPermissionRequest()
                    } else {
                        showLocationPermissionNeededDialog = true
                    }
                }
                is NextStep.NotificationPermissionNeeded -> {
                    if (nextStep.shouldRequest) {
                        notificationPermission.launchPermissionRequest()
                    } else {
                        showNotificationPermissionNeededDialog = true
                    }
                }
                is NextStep.MockLocationSettingNeeded -> {
                    showMockLocationSettingDialog = true
                }
                is NextStep.Ready -> {
                    setState(true)
                    tryStart = 0
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                showSwitch = fakeLocation != null,
                state = state,
                onSwitchChecked = { checked ->
                    if (checked) {
                        tryStart++
                    } else {
                        tryStart = 0
                        setState(false)
                    }
                }
            )
        }
    ) { p ->
        Box(modifier = Modifier.padding(p)) {
            Map(
                cameraPositionState = cameraPositionState,
                hasLocationPermission = locationPermission.status.isGranted,
                fakeLocation = fakeLocation,
                setFakeLocation = setFakeLocation
            )

            SearchBar(
                cameraPositionState = cameraPositionState,
                fakeLocation = fakeLocation,
                isFakeLocationSaved = isFakeLocationSaved,
                onClick = {
                    onSearchClick(cameraPositionState.bounds)
                },
                onCloseClick = {
                    setFakeLocation(null)
                },
                onStarClick = toggleSave
            )
        }
    }

    if (showLocationPermissionNeededDialog) {
        PermissionNeededDialog(
            permissionName = stringResource(R.string.location),
            onDismiss = {
                showLocationPermissionNeededDialog = false
            }
        )
    }

    if (showNotificationPermissionNeededDialog) {
        PermissionNeededDialog(
            permissionName = stringResource(R.string.notification),
            onDismiss = {
                showNotificationPermissionNeededDialog = false
            }
        )
    }

    if (showMockLocationSettingDialog) {
        MockLocationSettingDialog(
            onDismiss = {
                showMockLocationSettingDialog = false
            }
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
        OFF -> MaterialTheme.colors.surface
        ON -> pulseColor
    }
    val contentColor = when (state) {
        OFF -> MaterialTheme.colors.primaryOnSurface
        ON -> MaterialTheme.colors.onPrimary
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
                        onCheckedChange = onSwitchChecked,
                        colors = SwitchDefaults.colors(
                            uncheckedThumbColor = with(MaterialTheme.colors) {
                                val overlayAlpha = if (isLight) .12f else .24f
                                onSurface.copy(alpha = overlayAlpha).compositeOver(surface)
                            }
                        )
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
    cameraPositionState: CameraPositionState,
    fakeLocation: FakeLocation?,
    isFakeLocationSaved: Boolean,
    onClick: () -> Unit,
    onCloseClick: () -> Unit,
    onStarClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.padding(padding),
        shape = CircleShape,
        elevation = cardElevation()
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
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colors.primaryOnSurface,
                        strokeWidth = 2.dp
                    )
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

            var cameraIdleBounds by remember { mutableStateOf(cameraPositionState.bounds) }
            cameraPositionState.onCameraIdle {
                cameraIdleBounds = cameraPositionState.bounds
            }
            val isMarkerOffscreen by remember(cameraIdleBounds, fakeLocation) {
                derivedStateOf {
                    fakeLocation?.let { cameraIdleBounds?.contains(fakeLocation.latLng) } == false
                }
            }

            val coroutineScope = rememberCoroutineScope()
            FadeAnimatedVisibility(visible = fakeLocation != null) {
                CompositionLocalProvider(
                    LocalContentAlpha provides ContentAlpha.medium,
                    LocalMinimumInteractiveComponentEnforcement provides false
                ) {
                    Row {
                        FadeAnimatedVisibility(visible = isMarkerOffscreen) {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        fakeLocation?.latLng?.let { cameraPositionState.animateTo(it) }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(horizontal = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Place,
                                    contentDescription = stringResource(R.string.marker_location),
                                )
                            }
                        }
                        IconButton(
                            onClick = onCloseClick,
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.clear_fake_location)
                            )
                        }
                        IconButton(
                            onClick = onStarClick,
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 8.dp)
                        ) {
                            if (isFakeLocationSaved) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = stringResource(R.string.added_to_favorites),
                                    tint = with(MaterialTheme.colors) {
                                        primaryOnSurface.copy(if (isLight) ContentAlpha.high else ContentAlpha.medium)
                                    }
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.StarOutline,
                                    contentDescription = stringResource(R.string.add_to_favorites)
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
    hasLocationPermission: Boolean,
    fakeLocation: FakeLocation?,
    setFakeLocation: (FakeLocation?) -> Unit,
) {
    val properties = remember(hasLocationPermission) {
        MapProperties(isMyLocationEnabled = hasLocationPermission)
    }
    val uiSettings = remember {
        MapUiSettings(mapToolbarEnabled = false, tiltGesturesEnabled = false)
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
            mapColorScheme = ComposeMapColorScheme.FOLLOW_SYSTEM,
        ) {
            if (fakeLocation != null) {
                val fakeLocationPosition = fakeLocation.latLng
                Marker(state = remember(fakeLocationPosition) { MarkerState(fakeLocationPosition) })

                // If position of new fake location is outside of the camera bounds (mostly likely because we searched for it),
                // animate to the new point.
                LaunchedEffect(fakeLocationPosition) {
                    if (cameraPositionState.bounds?.contains(fakeLocationPosition) == false) {
                        cameraPositionState.animateTo(fakeLocationPosition)
                    }
                }
            }
        }
    }
}

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MainLayoutPreview() {
    val fakeLocation = FakeLocation(
        name = "Hogwarts",
        latitude = 57.388222,
        longitude = 3.711944
    )
    val myLatLng = LatLng(57.388322, 3.712944)
    AppTheme {
        MainLayout(
            fakeLocation = fakeLocation,
            isFakeLocationSaved = true,
            state = OFF,
            nextStep = NextStep.Ready,
            setLocationPermissionStatus = { },
            setNotificationPermissionStatus = { },
            setFakeLocation = { },
            setState = { },
            toggleSave = { },
            getLastLocation = { null },
            onSearchClick = { }
        )
    }
}