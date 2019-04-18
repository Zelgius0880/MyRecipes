package zelgius.com.myrecipes.repository

import android.content.Context
import androidx.room.*
import zelgius.com.myrecipes.entities.Ingredient
import zelgius.com.myrecipes.entities.Recipe
import zelgius.com.myrecipes.repository.dao.IngredientDao
import zelgius.com.myrecipes.repository.dao.RecipeDao

@Database(entities = [Ingredient::class, Recipe::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract val ingredientDao: IngredientDao
    abstract val recipeDao: RecipeDao

    companion object {
        private var instance: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            if (instance == null) instance = Room.databaseBuilder(
                context,
                AppDatabase::class.java, "database"
            ).build()

            return instance!!
        }
    }
}

class Converters {
    @TypeConverter
    fun unitToString(unit: Ingredient.Unit): String = unit.name

    @TypeConverter
    fun stringToUnit(s: String): Ingredient.Unit = Ingredient.Unit.valueOf(s)
}