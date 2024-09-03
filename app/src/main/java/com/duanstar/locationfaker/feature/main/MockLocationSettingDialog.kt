package com.duanstar.locationfaker.feature.main

import android.content.Intent
import android.content.res.Configuration
import android.provider.Settings
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.duanstar.locationfaker.R
import com.duanstar.locationfaker.ui.theme.AppTheme

@Composable
fun MockLocationSettingDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            val context = LocalContext.current
            val isDeveloperOptionsEnabled =
                Settings.Secure.getInt(context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) != 0
            TextButton(
                onClick = {
                    val intent = Intent(
                        if (isDeveloperOptionsEnabled) {
                            Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS
                        } else {
                            Settings.ACTION_SETTINGS
                        }
                    )
                    context.startActivity(intent)
                    onDismiss()
                }
            ) {
                Text(text = stringResource(if (isDeveloperOptionsEnabled) R.string.developer_options else R.string.settings).uppercase())
            }
        },
        text = {
            Text(text = stringResource(R.string.enable_mock_locations_message))
        }
    )
}

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EnableMockLocationSettingDialogPreview() {
    AppTheme {
        MockLocationSettingDialog(onDismiss = {})
    }
}
