package com.duanstar.locationfaker.permission

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalInspectionMode
import com.duanstar.locationfaker.BuildConfig
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun requireLocationPermission(): MultiplePermissionsState {
    if (BuildConfig.DEBUG && LocalInspectionMode.current) {
        return mockLocationPermission(true)
    }

    val state = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )

    LaunchedEffect(Unit) {
        state.launchMultiplePermissionRequest()
    }

    return state
}

@OptIn(ExperimentalPermissionsApi::class)
val MultiplePermissionsState.anyGranted: Boolean
    //    get() = revokedPermissions.size < permissions.size
    get() = permissions.any { permissionState ->
        permissionState.status.isGranted
    }

@OptIn(ExperimentalPermissionsApi::class)
private fun mockLocationPermission(granted: Boolean) = object : MultiplePermissionsState {

    private val mockPermissionState = object : PermissionState {
        override val permission = Manifest.permission.ACCESS_COARSE_LOCATION
        override val status = if (granted) PermissionStatus.Granted else PermissionStatus.Denied(true)

        override fun launchPermissionRequest() {}
    }

    override val allPermissionsGranted = granted
    override val permissions: List<PermissionState> = listOf(mockPermissionState)
    override val revokedPermissions: List<PermissionState> = if (granted) emptyList() else listOf(mockPermissionState)
    override val shouldShowRationale: Boolean = true

    override fun launchMultiplePermissionRequest() {}
}
