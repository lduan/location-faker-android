package com.duanstar.locationfaker.feature.favorites

import android.content.SharedPreferences
import androidx.core.content.edit
import com.duanstar.locationfaker.fake_location.FakeLocation
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
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

    private val adapter: JsonAdapter<List<FakeLocation>> by lazy {
        val type = Types.newParameterizedType(List::class.java, FakeLocation::class.java)
        Moshi.Builder()
            .build()
            .adapter(type)
    }
    private val _favorites = MutableStateFlow(read())

    val favorites = _favorites.asStateFlow()

    init {
        // Write changes to favorites to shared prefs
        coroutineScope.launch {
            _favorites.drop(1).collect(::write)
        }
    }

    fun addOrRemove(fakeLocation: FakeLocation) {
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
                adapter.fromJson(json)
            } catch (e: IOException) {
                Timber.e(e, "Failed to read favorites. json=$json")
                null
            } catch (e: JsonDataException) {
                Timber.e(e, "Failed to read favorites. json=$json")
                null
            }
        }.orEmpty()
    }

    private fun write(value: List<FakeLocation>) {
        prefs.edit {
            putString(PREF_KEY, adapter.toJson(value))
        }
    }
}