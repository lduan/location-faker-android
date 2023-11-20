package com.duanstar.locationfaker.feature.main

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.duanstar.locationfaker.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionRequiredDialog(
    permissionState: MultiplePermissionsState,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            val context = LocalContext.current
            TextButton(onClick = {
                if (permissionState.shouldShowRationale) {
                    permissionState.launchMultiplePermissionRequest()
                } else {
                    val uriPackage = Uri.fromParts("package", context.packageName, null)
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uriPackage)
                    context.startActivity(intent)
                    onDismiss()
                }
            }) {
                Text(text = stringResource(R.string.grant).uppercase())
            }
        },
        text = {
            Text(text = stringResource(R.string.enable_locations_message))
        }
    )
}
