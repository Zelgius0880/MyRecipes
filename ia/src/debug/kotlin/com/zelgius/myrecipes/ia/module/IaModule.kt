package com.zelgius.myrecipes.ia.module

import com.zelgius.myrecipes.ia.usecase.DataExtractionUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import zelgius.com.myrecipes.data.repository.IngredientRepository

@Module
@InstallIn(SingletonComponent::class)
class IaModule {
    @Provides
    fun provideDataExtractionUseCase(ingredientRepository: IngredientRepository) =
        object : DataExtractionUseCase(ingredientRepository) {
            /*override val callable = Firebase.functions
                .getHttpsCallableFromUrl(URL("http://10.0.2.2:5001/piclock-c9af5/europe-west2/extractRecipe"))*/
        }
}