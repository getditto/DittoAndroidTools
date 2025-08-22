package live.ditto.dittotoolsapp.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import live.ditto.dittotoolsapp.service.CredentialsService
import live.ditto.dittotoolsapp.service.DittoService
import live.ditto.dittotoolsapp.service.AuthService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ServiceModule {

    @Provides
    @Singleton
    fun provideAuthService(dittoService: DittoService, credentialsService: CredentialsService): AuthService {
        val authService = AuthService(credentialsService)
        authService.initialize(dittoService)
        return authService
    }
}