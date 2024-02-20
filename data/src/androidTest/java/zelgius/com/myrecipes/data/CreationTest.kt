package zelgius.com.myrecipes.data/*
package zelgius.com.myrecipes.repository

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.impl.utils.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@RunWith(AndroidJUnit4::class)
class CreationTest : AbstractDatabaseTest() {
    @Before
    fun setup() {
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()

        // Initialize WorkManager for instrumentation tests.
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    @Test
    fun checkIfDefaultCreated() {
        runBlocking {
            suspendCoroutine<Unit> { cont ->
                db.workerState.observeOnce {
                    it.get()
                    cont.resume(Unit)
                }
            }

            Assert.assertEquals(db.recipeDao.blockingGetAll().size, 3)
        }
    }
}*/
