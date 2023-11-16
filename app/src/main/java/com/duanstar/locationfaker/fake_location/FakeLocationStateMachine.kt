package com.duanstar.locationfaker.fake_location

import com.duanstar.locationfaker.settings.MockLocationSetting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeLocationStateMachine @Inject constructor(
    private val coroutineScope: CoroutineScope,
    private val fakeLocationStream: FakeLocationStream,
    private val mockLocationSetting: MockLocationSetting
) {

    private val _state = MutableStateFlow(State.OFF)

    val state = _state.asStateFlow()

    init {
        coroutineScope.launch {
            // Turn off state if fake location becomes null
            fakeLocationStream.fakeLocation.collect { fakeLocation ->
                fakeLocation ?: off()
            }
        }
        coroutineScope.launch {
            // Turn off state if mock locations are disabled in developer options
            mockLocationSetting.enabled.collect { enabled ->
                if (!enabled) off()
            }
        }
    }

    fun on() {
        _state.value = State.ON
    }

    fun off() {
        _state.value = State.OFF
    }

    enum class State {
        OFF, ON
    }
}
