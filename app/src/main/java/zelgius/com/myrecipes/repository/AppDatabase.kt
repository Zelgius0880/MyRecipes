package zelgius.com.myrecipes.repository

import android.content.ContentValues
import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import zelgius.com.myrecipes.entities.*
import zelgius.com.myrecipes.repository.dao.IngredientDao
import zelgius.com.myrecipes.repository.dao.RecipeDao
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.repository.dao.StepDao


@Database(
    entities = [Ingredient::class, Recipe::class, Step::class, RecipeIngredient::class],
    views = [IngredientForRecipe::class],
    version = 2
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract val ingredientDao: IngredientDao
    abstract val recipeDao: RecipeDao
    abstract val stepDao: StepDao

    companion object {
        private var instance: AppDatabase? = null
        fun getInstance(context: Context, test: Boolean = false): AppDatabase {
            if (instance == null) {
                instance = if (!test)
                    Room.databaseBuilder(
                        context,
                        AppDatabase::class.java, "database"
                    )
                        .fallbackToDestructiveMigration()
                        .addCallback(object : Callback(){
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            
                            db.insert("Ingredient", OnConflictStrategy.IGNORE, ContentValues().apply { 
                                put("name", context.getString(R.string.egg_name))
                                put("image_url", "drawable://egg")
                            })

                            db.insert("Ingredient", OnConflictStrategy.IGNORE, ContentValues().apply { 
                                put("name", context.getString(R.string.flour_name))
                                put("image_url", "drawable://flour")
                            })

                            db.insert("Ingredient", OnConflictStrategy.IGNORE, ContentValues().apply { 
                                put("name", context.getString(R.string.butter_name))
                                put("image_url", "drawable://butter")
                            })

                            db.insert("Ingredient", OnConflictStrategy.IGNORE, ContentValues().apply {
                                put("name", context.getString(R.string.sugar_name))
                                put("image_url", "drawable://sugar")
                            })

                            db.insert("Ingredient", OnConflictStrategy.IGNORE, ContentValues().apply { 
                                put("name", context.getString(R.string.water_name))
                                put("image_url", "drawable://water")
                            })

                            db.insert("Ingredient", OnConflictStrategy.IGNORE, ContentValues().apply {
                                put("name", context.getString(R.string.milk_name))
                                put("image_url", "drawable://milk")
                            })

                            db.insert("Ingredient", OnConflictStrategy.IGNORE, ContentValues().apply {
                                put("name", context.getString(R.string.salt_name))
                                put("image_url", "drawable://salt")
                            })

                        }
                    }).build()
                else
                    Room.inMemoryDatabaseBuilder(
                        context, AppDatabase::class.java
                    ).build()
            }

            return instance!!
        }
    }
}

class Converters {
    @TypeConverter
    fun unitToString(unit: Ingredient.Unit): String = unit.name

    @TypeConverter
    fun stringToUnit(s: String): Ingredient.Unit = Ingredient.Unit.valueOf(s)

    @TypeConverter
    fun typeToString(type: Recipe.Type): String = type.name

    @TypeConverter
    fun stringToType(s: String): Recipe.Type = Recipe.Type.valueOf(s)
}