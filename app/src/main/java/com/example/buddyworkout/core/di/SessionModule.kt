package com.example.buddyworkout.core.di

import com.example.buddyworkout.core.navigation.SessionSource
import com.example.buddyworkout.data.auth.FirebaseSessionSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SessionModule {

    @Binds
    @Singleton
    abstract fun sessionSource(impl: FirebaseSessionSource): SessionSource
}
