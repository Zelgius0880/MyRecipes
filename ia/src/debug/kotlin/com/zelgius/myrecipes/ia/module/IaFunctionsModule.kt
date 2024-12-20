package com.zelgius.myrecipes.ia.module

import android.os.Build
import com.google.firebase.Firebase
import com.google.firebase.functions.functions
import com.zelgius.myrecipes.ia.BuildConfig
import com.zelgius.myrecipes.ia.usecase.DataExtractionUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import zelgius.com.myrecipes.data.repository.IngredientRepository
import java.net.URL
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class IaFunctionsModule {
    @Provides
    @Singleton
    fun provideDataExtractionUseCase(ingredientRepository: IngredientRepository) =
        object : DataExtractionUseCase(ingredientRepository) {
            override val callable = Firebase.functions.let {
                if (Build.FINGERPRINT.contains("generic"))
                    it.getHttpsCallableFromUrl(URL("http://10.0.2.2:5001/piclock-c9af5/europe-west2/extractRecipe"))
                else it.getHttpsCallableFromUrl(URL(BuildConfig.CLOUD_FUNCTION_URL))

            }
        }
}