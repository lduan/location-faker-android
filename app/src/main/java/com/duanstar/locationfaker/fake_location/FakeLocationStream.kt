package com.duanstar.locationfaker.fake_location

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stream that allows for observing and updating the current fake location.
 */
@Singleton
class FakeLocationStream @Inject constructor() {

    private val _fakeLocation = MutableStateFlow<FakeLocation?>(null)

    val fakeLocation = _fakeLocation.asStateFlow()

    fun update(value: FakeLocation?) {
        _fakeLocation.value = value
    }
}
