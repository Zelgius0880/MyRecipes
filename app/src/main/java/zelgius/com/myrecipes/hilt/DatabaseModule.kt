package zelgius.com.myrecipes.hilt

import android.content.Context
import androidx.core.content.contentValuesOf
import androidx.room.OnConflictStrategy
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import zelgius.com.myrecipes.data.AppDatabase
import zelgius.com.myrecipes.data.DefaultIngredients
import zelgius.com.myrecipes.data.repository.IngredientRepository
import zelgius.com.myrecipes.data.repository.RecipeRepository
import zelgius.com.myrecipes.data.repository.StepRepository
import zelgius.com.myrecipes.data.repository.dao.IngredientDao
import zelgius.com.myrecipes.data.repository.dao.RecipeDao
import zelgius.com.myrecipes.data.repository.dao.StepDao
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {

    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context) = AppDatabase.Companion.createDatabase(context) { db ->
        DefaultIngredients.entries.forEach {
            insertOrUpdateIngredient(context, db, it)
        }
    }

    @Provides
    @Singleton
    fun ingredientDao(appDatabase: AppDatabase) = appDatabase.ingredientDao

    @Provides
    @Singleton
    fun stepDao(appDatabase: AppDatabase) = appDatabase.stepDao

    @Provides
    @Singleton
    fun recipeDao(appDatabase: AppDatabase) = appDatabase.recipeDao

    @Provides
    @Singleton
    fun imageGenerationProgressDao(appDatabase: AppDatabase) = appDatabase.imageGenerationProgressDao

    @Provides
    @Singleton
    fun recipeRepository(recipeDao: RecipeDao, stepDao: StepDao, ingredientDao: IngredientDao) =
        RecipeRepository(recipeDao, stepDao, ingredientDao)

    @Provides
    @Singleton
    fun stepRepository(stepDao: StepDao) = StepRepository(stepDao)

    @Provides
    @Singleton
    fun ingredientRepository(ingredientDao: IngredientDao) = IngredientRepository(ingredientDao)

    companion object {
        private fun insertOrUpdateIngredient(
            context: Context,
            db: SupportSQLiteDatabase,
            item: DefaultIngredients
        ) {
            val name = context.getString(item.string)
            val contentValues = contentValuesOf(
                "name" to name,
                "image_url" to item.url

            )
            if (db.update(
                    "Ingredient",
                    OnConflictStrategy.REPLACE,
                    contentValues,
                    "name = ?",
                    arrayOf(name)
                ) == 0
            ) {
                db.insert("Ingredient", OnConflictStrategy.IGNORE, contentValues)
            }
        }
    }
}