package com.example.weedx.di

import android.content.SharedPreferences
import com.example.weedx.data.api.AuthApiService
import com.example.weedx.data.repositories.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing repository instances
 * Currently only Auth is implemented
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
}
