package com.duanstar.locationfaker.di

import android.app.NotificationManager
import android.app.Service
import android.content.Context.NOTIFICATION_SERVICE
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
class ServiceModule {

    @Provides
    @ServiceScoped
    fun provideNotificationManager(service: Service): NotificationManager {
        return service.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }
}
