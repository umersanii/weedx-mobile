package com.example.weedx.di

import com.example.weedx.data.api.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Hilt module for providing API service instances
 */
@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    
    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideDashboardApiService(retrofit: Retrofit): DashboardApiService {
        return retrofit.create(DashboardApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideMonitoringApiService(retrofit: Retrofit): MonitoringApiService {
        return retrofit.create(MonitoringApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideWeedLogsApiService(retrofit: Retrofit): WeedLogsApiService {
        return retrofit.create(WeedLogsApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideEnvironmentApiService(retrofit: Retrofit): EnvironmentApiService {
        return retrofit.create(EnvironmentApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideReportsApiService(retrofit: Retrofit): ReportsApiService {
        return retrofit.create(ReportsApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideGalleryApiService(retrofit: Retrofit): GalleryApiService {
        return retrofit.create(GalleryApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideProfileApiService(retrofit: Retrofit): ProfileApiService {
        return retrofit.create(ProfileApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideAssistantApiService(retrofit: Retrofit): AssistantApiService {
        return retrofit.create(AssistantApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideFcmTokenApiService(retrofit: Retrofit): FcmTokenApiService {
        return retrofit.create(FcmTokenApiService::class.java)
    }
}
