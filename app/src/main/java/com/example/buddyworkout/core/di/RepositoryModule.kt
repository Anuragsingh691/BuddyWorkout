package com.example.buddyworkout.core.di

import com.example.buddyworkout.data.auth.AuthRepository
import com.example.buddyworkout.data.auth.AuthRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Per `docs/architecture/04-lld-android.md` §3. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun authRepository(impl: AuthRepositoryImpl): AuthRepository
}
