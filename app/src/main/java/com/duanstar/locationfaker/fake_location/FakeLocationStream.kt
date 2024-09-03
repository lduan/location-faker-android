package com.duanstar.locationfaker.fake_location

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stream that allows for observing and updating the current fake location.
 */
@Singleton
class FakeLocationStream @Inject constructor(
    private val coroutineScope: CoroutineScope,
    private val prefs: SharedPreferences
) {
    companion object {
        private const val PREF_KEY = "fake_location"
    }

    private val _fakeLocation: MutableStateFlow<FakeLocation?> = MutableStateFlow(read())
    val fakeLocation = _fakeLocation.asStateFlow()

    init {
        // Persist current fake location in shared prefs
        coroutineScope.launch {
            _fakeLocation.drop(1).collect(::write)
        }
    }

    fun update(value: FakeLocation?) {
        _fakeLocation.value = value
    }

    private fun read(): FakeLocation? {
        return prefs.getString(PREF_KEY, null)?.let { json ->
            try {
                Json.decodeFromString<FakeLocation>(json)
            } catch (e: SerializationException) {
                Timber.e(e, "Failed to read fake location. json=$json")
                null
            }
        }
    }

    private fun write(value: FakeLocation?) {
        prefs.edit {
            putString(PREF_KEY, value?.let {
                Json.encodeToString<FakeLocation>(it)
            })
        }
    }
}
