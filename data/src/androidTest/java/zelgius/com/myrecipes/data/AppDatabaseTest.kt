package zelgius.com.myrecipes.data

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import zelgius.com.myrecipes.data.repository.AppDatabase

const val TEST_DB_NAME = "test_db.db"

class AppDatabaseTest {

    @get:Rule
    val testHelper = MigrationTestHelper(
    InstrumentationRegistry.getInstrumentation(),
    AppDatabase::class.java.canonicalName, FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun runMigration1_2(){
        // Create the database with version 2
        val db = testHelper.createDatabase(TEST_DB_NAME, 1)

        db.close()

        testHelper.runMigrationsAndValidate(TEST_DB_NAME, 2, true , AppDatabase.MIGRATION_1_2)
    }
}
