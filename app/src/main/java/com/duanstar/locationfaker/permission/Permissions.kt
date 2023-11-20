@file:OptIn(ExperimentalPermissionsApi::class)

package com.duanstar.locationfaker.permission

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalInspectionMode
import com.duanstar.locationfaker.BuildConfig
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@Composable
fun rememberLocationPermission(): MultiplePermissionsState {
    return if (BuildConfig.DEBUG && LocalInspectionMode.current) {
        mockMultiplePermissionState("", granted = true, shouldShowRationale = true)
    } else {
        rememberMultiplePermissionsState(
            listOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }
}

@Composable
fun rememberNotificationsPermission(): PermissionState {
    return if (BuildConfig.DEBUG && LocalInspectionMode.current) {
        mockPermissionState("", granted = true, shouldShowRationale = true)
    } else {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    }
}

val MultiplePermissionsState.anyGranted: Boolean
    get() = permissions.any { it.status.isGranted }

private fun mockMultiplePermissionState(permission: String, granted: Boolean, shouldShowRationale: Boolean) =
    object : MultiplePermissionsState {

        private val permissionStates = listOf(mockPermissionState(permission, granted, shouldShowRationale))

        override val allPermissionsGranted = granted
        override val permissions: List<PermissionState> = permissionStates
        override val revokedPermissions: List<PermissionState> = if (granted) emptyList() else permissionStates
        override val shouldShowRationale: Boolean = permissionStates.any { it.status.shouldShowRationale }

        override fun launchMultiplePermissionRequest() {}
    }

private fun mockPermissionState(permission: String, granted: Boolean, shouldShowRationale: Boolean) =
    object : PermissionState {

        override val permission = permission
        override val status = if (granted) PermissionStatus.Granted else PermissionStatus.Denied(shouldShowRationale)

        override fun launchPermissionRequest() {}
    }