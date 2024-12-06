package zelgius.com.myrecipes

import android.app.Application
import android.content.Context
import androidx.core.content.contentValuesOf
import androidx.room.OnConflictStrategy
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import zelgius.com.myrecipes.data.AppDatabase
import zelgius.com.myrecipes.data.DefaultIngredients
import java.io.IOException

object TestDataBase {
    private var _db: AppDatabase? = null
    val context by lazy { ApplicationProvider.getApplicationContext<Application>()!! }
    val db get() = _db!!

    fun createDb() {
        _db = AppDatabase.getInstance(context, true) { db ->
            DefaultIngredients.entries.forEach {
                insertOrUpdateIngredient(context, db, it)
            }
        }
    }

    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }


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