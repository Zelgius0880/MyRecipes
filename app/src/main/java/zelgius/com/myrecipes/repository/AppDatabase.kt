package zelgius.com.myrecipes.repository

import android.content.ContentValues
import android.content.Context
import androidx.core.content.contentValuesOf
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
    version = 3
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
                    ).apply {
                        if (test) allowMainThreadQueries()
                    }
                        .fallbackToDestructiveMigration()
                        .addCallback(object : Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                super.onCreate(db)

                                DefaultIngredients.values().forEach {
                                    insertOrUpdateIngredient(context,db, it)
                                }

                            }
                        }).build()
                else
                    Room.inMemoryDatabaseBuilder(
                        context, AppDatabase::class.java
                    ).build()
            }

            return instance!!
        }

        fun insertOrUpdateIngredient(
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