package com.duanstar.locationfaker.feature.main

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.provider.Settings
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.duanstar.locationfaker.R
import com.duanstar.locationfaker.permission.rememberLocationPermission
import com.duanstar.locationfaker.ui.theme.AppTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionRequiredDialog(
    onDismiss: () -> Unit
) {
    val locationPermissionState = rememberLocationPermission()
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            val context = LocalContext.current
            TextButton(onClick = {
                if (locationPermissionState.shouldShowRationale) {
                    locationPermissionState.launchMultiplePermissionRequest()
                } else {
                    val uriPackage = Uri.fromParts("package", context.packageName, null)
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uriPackage)
                    context.startActivity(intent)
                }
                onDismiss()
            }) {
                Text(text = stringResource(R.string.grant).uppercase())
            }
        },
        text = {
            Text(text = stringResource(R.string.enable_locations_message))
        }
    )
}

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LocationPermissionRequiredDialogPreview() {
    AppTheme {
        LocationPermissionRequiredDialog(
            onDismiss = {}
        )
    }
}
