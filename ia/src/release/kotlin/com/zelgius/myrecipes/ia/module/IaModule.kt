package com.zelgius.myrecipes.ia.module

import com.google.firebase.Firebase
import com.google.firebase.functions.functions
import com.zelgius.myrecipes.ia.usecase.DataExtractionUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import zelgius.com.myrecipes.data.repository.IngredientRepository
import java.net.URL

@Module
@InstallIn(SingletonComponent::class)
class IaModule {
    @Provides
    fun provideDataExtractionUseCase(ingredientRepository: IngredientRepository) = DataExtractionUseCase(ingredientRepository)
}