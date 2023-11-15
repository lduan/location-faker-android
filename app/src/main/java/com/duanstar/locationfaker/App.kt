package com.duanstar.locationfaker

import android.app.Application
import com.duanstar.locationfaker.fake_location.FakeLocationService
import com.duanstar.locationfaker.fake_location.FakeLocationStateMachine
import com.duanstar.locationfaker.fake_location.FakeLocationStateMachine.State
import com.duanstar.locationfaker.fake_location.FakeLocationStream
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    @Inject lateinit var coroutineScope: CoroutineScope
    @Inject lateinit var stateMachine: FakeLocationStateMachine
    @Inject lateinit var stream: FakeLocationStream

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        coroutineScope.launch {
            combine(stateMachine.state, stream.fakeLocation) { state, fakeLocation ->
                // Start service when switch is turned on and we have a fake location.
                state == State.ON && fakeLocation != null
            }
                .distinctUntilChanged()
                .collectLatest { startService ->
                    if (startService) {
                        FakeLocationService.start(this@App)
                    } else {
                        FakeLocationService.stop(this@App)
                    }
                }
        }
    }
}