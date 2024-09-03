@file:OptIn(ExperimentalPermissionsApi::class)

package com.duanstar.locationfaker.permission

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalInspectionMode
import com.duanstar.locationfaker.BuildConfig
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@Composable
fun rememberLocationPermission(onPermissionResult: (Boolean) -> Unit = {}): PermissionState {
    return if (isPreview()) {
        mockGrantedPermission()
    } else {
        rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION, onPermissionResult)
    }
}

@Composable
fun rememberNotificationsPermission(onPermissionResult: (Boolean) -> Unit = {}): PermissionState {
    return if (isPreview()) {
        mockGrantedPermission()
    } else {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS, onPermissionResult)
    }
}

val PermissionStatus.isNotGranted
    get() = !isGranted

@Composable
private fun isPreview(): Boolean = BuildConfig.DEBUG && LocalInspectionMode.current

@Composable
private fun mockGrantedPermission() = object : PermissionState {

    override val permission = ""
    override val status = PermissionStatus.Granted

    override fun launchPermissionRequest() {}
}
