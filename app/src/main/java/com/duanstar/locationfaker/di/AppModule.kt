package com.duanstar.locationfaker.di

import android.content.Context
import android.content.SharedPreferences
import android.location.Geocoder
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.preference.PreferenceManager
import com.duanstar.locationfaker.BuildConfig
import com.duanstar.locationfaker.fake_location.FakeLocationStream
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.compose.CameraPositionState
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import javax.inject.Singleton

@Suppress("PrivatePropertyName")
private val SAN_FRANCISCO_LAT_LNG = LatLng(37.7749, -122.4194)

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideCameraPositionState(fakeLocationStream: FakeLocationStream): CameraPositionState {
        val fakeLocation = fakeLocationStream.fakeLocation.value
        return CameraPositionState().apply {
            position = CameraPosition.fromLatLngZoom(fakeLocation?.latLng ?: SAN_FRANCISCO_LAT_LNG, 15f)
        }
    }

    @Provides
    @Singleton
    fun provideMainScope(): CoroutineScope {
        return MainScope()
    }

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(@ApplicationContext context: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    @Provides
    @Singleton
    fun provideGeocoder(@ApplicationContext context: Context): Geocoder {
        return Geocoder(context)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor { message -> Timber.d(message) }.setLevel(HttpLoggingInterceptor.Level.BODY))
            }
        }.build()
    }

    @Provides
    @Singleton
    fun providePlacesClient(@ApplicationContext context: Context): PlacesClient {
        return Places.createClient(context)
    }

    @ProcessLifecycle
    @Provides
    @Singleton
    fun provideProcessLifecycle(): Lifecycle {
        return ProcessLifecycleOwner.get().lifecycle
    }

    @Provides
    @Singleton
    fun provideSharedPrefs(@ApplicationContext context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }
}