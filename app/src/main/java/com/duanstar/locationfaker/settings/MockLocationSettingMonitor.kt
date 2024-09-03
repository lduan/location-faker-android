package com.duanstar.locationfaker.settings

import android.app.AppOpsManager
import android.content.Context
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.duanstar.locationfaker.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockLocationSettingMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val _enabled = MutableStateFlow(isMockLocationEnabled())
    val enabled = _enabled.asStateFlow()

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            // Check if mock locations are enabled whenever we foreground the app.
            override fun onStart(owner: LifecycleOwner) {
                _enabled.value = isMockLocationEnabled()
            }
        })
    }

    private fun isMockLocationEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val manager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            try {
                val uid = Process.myUid()
                val appId = BuildConfig.APPLICATION_ID
                manager.checkOp(AppOpsManager.OPSTR_MOCK_LOCATION, uid, appId) == AppOpsManager.MODE_ALLOWED
            } catch (e: SecurityException) {
                false
            }
        } else {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ALLOW_MOCK_LOCATION) != "0"
        }
    }
}
