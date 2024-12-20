package zelgius.com.myrecipes.data.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import zelgius.com.myrecipes.data.repository.RemoteConfigRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataModule {

    @Provides
    @Singleton
    fun provideRemoteConfigRepository() = RemoteConfigRepository()

}