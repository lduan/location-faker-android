package com.duanstar.locationfaker.permission

import android.app.AppOpsManager
import android.content.Context
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.duanstar.locationfaker.BuildConfig
import com.duanstar.locationfaker.di.ProcessLifecycle
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockLocationSetting @Inject constructor(
    @ApplicationContext private val context: Context,
    @ProcessLifecycle private val processLifecycle: Lifecycle
) {

    private val _enabled = MutableStateFlow(isMockLocationEnabled(context))

    val enabled = _enabled.asStateFlow()

    init {
        processLifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                this@MockLocationSetting._enabled.value = isMockLocationEnabled(context)
            }
        })
    }

    /**
     * Check if the app has the permission to mock locations.
     *
     * @return `true` if the app can mock locations; otherwise, `false`.
     */
    private fun isMockLocationEnabled(context: Context): Boolean {
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