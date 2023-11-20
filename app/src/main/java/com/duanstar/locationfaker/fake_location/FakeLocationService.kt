package com.duanstar.locationfaker.fake_location

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.duanstar.locationfaker.R
import com.duanstar.locationfaker.feature.main.MainActivity
import com.duanstar.locationfaker.launch
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filterNotNull
import timber.log.Timber
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.timer

@AndroidEntryPoint
class FakeLocationService : LifecycleService() {

    companion object {

        private const val NOTIFICATION_CHANNEL_ID = "service"
        private const val NOTIFICATION_ID = 496

        fun start(context: Context) {
            Intent(context, FakeLocationService::class.java).let(context::startService)
        }

        fun stop(context: Context) {
            Intent(context, FakeLocationService::class.java).let(context::stopService)
        }
    }

    @Inject lateinit var fakeLocationStream: FakeLocationStream
    @Inject lateinit var locationClient: FusedLocationProviderClient
    @Inject lateinit var notificationManager: NotificationManager

    private var timer: Timer? = null

    override fun onCreate() {
        super.onCreate()

        launch {
            fakeLocationStream.fakeLocation.filterNotNull().collect { fakeLocation ->
                // Android OS will kill this service unless we schedule periodic updates.
                timer?.cancel()
                timer = timer(initialDelay = 0, period = 5 * 60 * 1000) {
                    try {
                        val location = Location("fakeLocationProvider").apply {
                            altitude = 0.0
                            accuracy = 1f
                            bearing = 0f
                            latitude = fakeLocation.latitude
                            longitude = fakeLocation.longitude
                            speed = 0f
                            time = System.currentTimeMillis()
                            elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
                        }
                        locationClient.setMockMode(true)
                        locationClient.setMockLocation(location)
                        startForegroundNotification(fakeLocation)
                    } catch (e: SecurityException) {
                        Timber.e(e, "Mock location permission is not granted.")
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            locationClient.setMockMode(false)
        } catch (e: SecurityException) {
            Timber.e(e, "Mock location permission is not granted.")
        }
    }

    private fun startForegroundNotification(fakeLocation: FakeLocation) {
        val intentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }
        // On notification tap
        val intent = MainActivity.newIntent(this)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, intentFlags)

        // On notification stop action tap
        val stopIntent = Intent(this, StopFakeLocationBroadcast::class.java)
        val stopPendingIntent = PendingIntent.getBroadcast(this, 0, stopIntent, intentFlags)
        val stopAction = NotificationCompat.Action.Builder(
            R.drawable.ic_close_24, getString(R.string.stop), stopPendingIntent
        ).build()

        // Notification channel
        val notificationTitle = getString(R.string.fake_location_enabled)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, notificationTitle, NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_place_24)
            .setColor(ContextCompat.getColor(this, R.color.color_primary))
            .setContentTitle(notificationTitle)
            .setContentText(fakeLocation.title)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(stopAction)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }
}