package zelgius.com.myrecipes.data.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import zelgius.com.myrecipes.data.entities.IngredientEntity
import zelgius.com.myrecipes.data.entities.IngredientForRecipe
import zelgius.com.myrecipes.data.entities.RecipeEntity
import zelgius.com.myrecipes.data.entities.RecipeIngredient
import zelgius.com.myrecipes.data.entities.StepEntity
import zelgius.com.myrecipes.data.repository.dao.IngredientDao
import zelgius.com.myrecipes.data.repository.dao.RecipeDao
import zelgius.com.myrecipes.data.repository.dao.StepDao


@Database(
    entities = [IngredientEntity::class, RecipeEntity::class, StepEntity::class, RecipeIngredient::class],
    views = [IngredientForRecipe::class],
    version = 4
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract val ingredientDao: IngredientDao
    abstract val recipeDao: RecipeDao
    abstract val stepDao: StepDao

    /*val workerState:
            LiveData<ListenableFuture<Operation.State.SUCCESS>>
        get() = _workerState

    private val _workerState:
            MutableLiveData<ListenableFuture<Operation.State.SUCCESS>> = MutableLiveData()
*/
    companion object {
        private var instance: AppDatabase? = null
        fun getInstance(context: Context, test: Boolean = false): AppDatabase {
            if (instance == null) {
                instance = if (!test) createDatabase(context)
                else
                    Room.inMemoryDatabaseBuilder(
                        context, AppDatabase::class.java
                    )
                        .addCallback(object : Callback() {
                            override fun onOpen(db: SupportSQLiteDatabase) {
                                super.onOpen(db)
                            }

                            override fun onCreate(db: SupportSQLiteDatabase) {
                                super.onCreate(db)

                                /*DefaultIngredients.values().forEach {
                                    insertOrUpdateIngredient(context, db, it)
                                }

                                val worker = OneTimeWorkRequestBuilder<InsertDefaultDataWorker>()
                                    .setConstraints(Constraints.NONE)
                                    .build()

                                instance?._workerState?.value = WorkManager
                                    .getInstance(context)
                                    .enqueue(worker)
                                    .result*/
                            }
                        })
                        .build()
            }

            return instance!!
        }

        fun createDatabase(context: Context, onCreate: (db: SupportSQLiteDatabase) -> Unit = {}): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java, "database"
            )
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {

                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)

                        onCreate(db)
                    }
                })
                .addMigrations(MIGRATION_1_2)
                .addMigrations(MIGRATION_2_3)
                .addMigrations(MIGRATION_3_4)
                .build()
        }


        val MIGRATION_1_2 = createMigration(1, 2) {
            it.execSQL("ALTER TABLE RecipeIngredient ADD COLUMN optional INTEGER DEFAULT 0")
        }

        private val MIGRATION_2_3 = createMigration(2, 3) {
            it.execSQL("DROP VIEW IngredientForRecipe")
            it.execSQL(
                """
CREATE VIEW `IngredientForRecipe` AS SELECT ri.quantity, ri.unit, ri.ref_recipe AS refRecipe, ri.ref_step AS refStep, ri.sort_order AS sortOrder,
i.name, i.id, i.image_url AS imageUrl, ri.optional FROM RecipeIngredient ri
INNER JOIN Ingredient i ON i.id = ri.ref_ingredient
"""
            )
        }

        private val MIGRATION_3_4 = createMigration(3, 4) {
            it.execSQL("ALTER TABLE Step ADD COLUMN optional INTEGER NOT NULL DEFAULT 0")
        }


        private fun createMigration(from: Int, to: Int, work: (SupportSQLiteDatabase) -> Unit) =
            object : Migration(from, to) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    work(database)
                }
            }
    }
}

class Converters {
    @TypeConverter
    fun unitToString(unit: IngredientEntity.Unit): String = unit.name

    @TypeConverter
    fun stringToUnit(s: String): IngredientEntity.Unit = IngredientEntity.Unit.valueOf(s)

    @TypeConverter
    fun typeToString(type: RecipeEntity.Type): String = type.name

    @TypeConverter
    fun stringToType(s: String): RecipeEntity.Type = RecipeEntity.Type.valueOf(s)
}
