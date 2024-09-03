package com.duanstar.locationfaker.feature.favorites

import android.content.SharedPreferences
import androidx.core.content.edit
import com.duanstar.locationfaker.fake_location.FakeLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesManager @Inject constructor(
    private val coroutineScope: CoroutineScope,
    private val prefs: SharedPreferences
) {

    companion object {
        private const val PREF_KEY = "favorites"
    }

    private val _favorites = MutableStateFlow(read())
    val favorites = _favorites.asStateFlow()

    init {
        // Write any changes to favorites to shared prefs
        coroutineScope.launch {
            _favorites.drop(1).collect(::write)
        }
    }

    fun add(fakeLocation: FakeLocation) {
        _favorites.update {
            if (it.contains(fakeLocation)) {
                it - fakeLocation
            } else {
                it + fakeLocation
            }
        }
    }

    fun remove(fakeLocation: FakeLocation) {
        _favorites.update {
            it - fakeLocation
        }
    }

    private fun read(): List<FakeLocation> {
        return prefs.getString(PREF_KEY, null)?.let { json ->
            try {
                Json.decodeFromString<List<FakeLocation>>(json)
            } catch (e: SerializationException) {
                Timber.e(e, "Failed to read json=$json")
                null
            }
        }.orEmpty()
    }

    private fun write(value: List<FakeLocation>) {
        prefs.edit {
            putString(PREF_KEY, Json.encodeToString(value))
        }
    }
}
