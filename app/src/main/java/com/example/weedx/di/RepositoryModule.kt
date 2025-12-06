package com.example.weedx.di

import android.content.Context
import android.content.SharedPreferences
import com.example.weedx.data.api.*
import com.example.weedx.data.repositories.*
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing repository instances
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideAuthRepository(
        authApiService: AuthApiService,
        firebaseAuth: FirebaseAuth,
        sharedPreferences: SharedPreferences
    ): AuthRepository {
        return AuthRepository(authApiService, firebaseAuth, sharedPreferences)
    }
    
    @Provides
    @Singleton
    fun provideDashboardRepository(
        dashboardApiService: DashboardApiService
    ): DashboardRepository {
        return DashboardRepository(dashboardApiService)
    }
    
    @Provides
    @Singleton
    fun provideMonitoringRepository(
        monitoringApiService: MonitoringApiService
    ): MonitoringRepository {
        return MonitoringRepository(monitoringApiService)
    }
    
    @Provides
    @Singleton
    fun provideWeedLogsRepository(
        weedLogsApiService: WeedLogsApiService
    ): WeedLogsRepository {
        return WeedLogsRepository(weedLogsApiService)
    }
    
    @Provides
    @Singleton
    fun provideEnvironmentRepository(
        environmentApiService: EnvironmentApiService
    ): EnvironmentRepository {
        return EnvironmentRepository(environmentApiService)
    }
    
    @Provides
    @Singleton
    fun provideReportsRepository(
        reportsApiService: ReportsApiService
    ): ReportsRepository {
        return ReportsRepository(reportsApiService)
    }
    
    @Provides
    @Singleton
    fun provideGalleryRepository(
        galleryApiService: GalleryApiService
    ): GalleryRepository {
        return GalleryRepository(galleryApiService)
    }
    
    @Provides
    @Singleton
    fun provideProfileRepository(
        profileApiService: ProfileApiService
    ): ProfileRepository {
        return ProfileRepository(profileApiService)
    }
    
    @Provides
    @Singleton
    fun provideAssistantRepository(
        assistantApiService: AssistantApiService
    ): AssistantRepository {
        return AssistantRepository(assistantApiService)
    }
    
    @Provides
    @Singleton
    fun provideFcmTokenRepository(
        fcmTokenApiService: FcmTokenApiService,
        @ApplicationContext context: Context
    ): FcmTokenRepository {
        return FcmTokenRepository(fcmTokenApiService, context)
    }
}
