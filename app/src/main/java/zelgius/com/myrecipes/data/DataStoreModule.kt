package zelgius.com.myrecipes.data

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import zelgius.com.myrecipes.data.repository.DataStoreRepository
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DataStoreModule {

    @Provides
    @Singleton
    fun dataStore(@ApplicationContext context: Context) = DataStoreRepository(context)

}