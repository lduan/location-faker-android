package com.duanstar.locationfaker.fake_location

import android.content.SharedPreferences
import androidx.core.content.edit
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stream that allows for observing and updating the current fake location.
 */
@OptIn(ExperimentalStdlibApi::class)
@Singleton
class FakeLocationStream @Inject constructor(
    private val coroutineScope: CoroutineScope,
    private val prefs: SharedPreferences
) {
    companion object {
        private const val PREF_KEY = "fake_location"
    }

    private val adapter = Moshi.Builder()
        .build()
        .adapter<FakeLocation>()

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
                adapter.fromJson(json)
            } catch (e: IOException) {
                Timber.e(e, "Failed to read fake location. json=$json")
                null
            } catch (e: JsonDataException) {
                Timber.e(e, "Failed to read fake location. json=$json")
                null
            }
        }
    }

    private fun write(value: FakeLocation?) {
        prefs.edit {
            putString(PREF_KEY, value?.let(adapter::toJson))
        }
    }
}
