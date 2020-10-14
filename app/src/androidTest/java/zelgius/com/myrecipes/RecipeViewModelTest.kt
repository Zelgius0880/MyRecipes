package zelgius.com.myrecipes

import android.app.Application
import android.os.Environment
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.facebook.stetho.Stetho
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.junit.MockitoRule
import zelgius.com.myrecipes.entities.Recipe
import zelgius.com.myrecipes.repository.observeOnce
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


@RunWith(MockitoJUnitRunner::class)
class RecipeViewModelTest {
    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val context: Application by lazy { ApplicationProvider.getApplicationContext()!! }

    private val viewModel: RecipeViewModel by lazy { RecipeViewModel(context) }

    @Before
    fun init() {
        Stetho.initializeWithDefaults(context)

    }

    @Test
    fun saveCurrentRecipe() {

        val latch = CountDownLatch(2)


        viewModel.createDummySample()

        viewModel.saveCurrentRecipe().observeForever {

            //assertTrue(b)

            viewModel.loadRecipe(viewModel.currentRecipe.id!!).observeForever { _ ->

                compareRecipe(it, viewModel.currentRecipe)
                latch.countDown()


                viewModel.currentRecipe.name = "Test Update"
                viewModel.currentRecipe.ingredients.removeAt(1)
                viewModel.currentRecipe.steps.removeAt(0)

                viewModel.saveCurrentRecipe().observeForever {

                    //assertTrue(b)

                    viewModel.loadRecipe(viewModel.currentRecipe.id!!).observeForever {
                        compareRecipe(it!!, viewModel.currentRecipe)

                        latch.countDown()
                    }
                }


            }

        }

        latch.await(30, TimeUnit.SECONDS)



        assertTrue(latch.count == 0L)
    }

    private fun compareRecipe(o1: Recipe?, o2: Recipe) {
        assertNotNull(o1!!)
        assertEquals(o1, o2)

        assertEquals(o1.steps.size, o2.steps.size)
        o1.steps.forEach { s ->
            assertNotNull(o2.steps.find { s == it })
        }

        assertEquals(o1.ingredients.size, o2.ingredients.size)
        o1.ingredients.forEach { i ->
            assertNotNull(o2.ingredients.find { i == it })
        }
    }


    @Test
    fun saveRecipe() {

        val latch = CountDownLatch(2)
        viewModel.createDummySample()

        viewModel.saveRecipe(viewModel.currentRecipe).observeForever { r ->

            //assertTrue(b)

            viewModel.loadRecipe(r.id!!).observeForever {

                assertNotNull(it!!)
                compareRecipe(it, r)
                latch.countDown()

                r.name = "Test Update"
                r.ingredients.removeAt(1)
                r.steps.removeAt(0)

                viewModel.saveCurrentRecipe().observeForever { r ->

                    //assertTrue(b)

                    viewModel.loadRecipe(viewModel.currentRecipe.id!!).observeForever {r1 ->
                        compareRecipe(r1!!, r)

                        latch.countDown()

                        Thread.sleep(5000)

                        assertTrue(
                            File(
                                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                                "${r1.id}"
                            )
                                .exists()
                        )
                    }
                }
            }
        }

        latch.await(30, TimeUnit.SECONDS)
        assertTrue(latch.count == 0L)

    }

    @Test
    fun delete() {
        val latch = CountDownLatch(1)
        viewModel.createDummySample()

        viewModel.saveRecipe(viewModel.currentRecipe).observeForever { r ->

            //assertTrue(b)
            Thread.sleep(5000)

            viewModel.delete(r).observeForever { b ->

                if (b) {
                    viewModel.loadRecipe(r.id!!).observeForever {
                        assertNull(it)
                        latch.countDown()
                    }
                }
            }
        }

        latch.await(30, TimeUnit.SECONDS)
        assertTrue(latch.count == 0L)
    }

    @Test
    fun search() {
        var latch = CountDownLatch(1)
        viewModel.createDummySample()


        viewModel.saveRecipe(viewModel.currentRecipe).observeForever {
            latch.countDown()
        }

        latch.await(30, TimeUnit.SECONDS)
        assertTrue(latch.count == 0L)

        latch = CountDownLatch(2)

        viewModel.searchResult.observeForever {
            assertTrue(it.size > 0)
            latch.countDown()
        }

        viewModel.search("Test").observeForever {
            assertTrue(it.size > 0)
            latch.countDown()
        }
        latch.await(30, TimeUnit.SECONDS)
        assertTrue(latch.count == 0L)


    }

    @Rule
    @JvmField
    val mRuntimePermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!!

    @Rule
    @JvmField
    val mActivityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    @Test
    fun exportToPdf() {
        val file = File("/storage/emulated/0/Download")
        viewModel.createDummySample()
        viewModel.currentRecipe.imageURL =
            "file:/storage/emulated/0/Android/data/zelgius.com.myrecipes/files/Pictures/23"
        val latch = CountDownLatch(1)

        viewModel.exportToPdf(viewModel.currentRecipe, file).observeOnce {
            assertTrue(file.exists())

            latch.count
        }

        latch.await(10, TimeUnit.SECONDS)


        /*val target = Intent(Intent.ACTION_VIEW)
        target.setDataAndType(file.toUri(), "application/pdf")
        target.flags = Intent.FLAG_ACTIVITY_NEW_TASK // FLAG_ACTIVITY_NEW_TASK only for testing

        val intent = Intent.createChooser(target, "Open File")
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            fail(e.message)
        }*/
    }

    @Test
    fun saveFromQrCode() {

        var recipe = Recipe()
        wait(viewModel.saveFromQrCode(BASE_64_RECIPE)){
            assertNotNull(it)

            recipe = it!!
        }

        wait(viewModel.loadRecipe(recipe.id!!)){
            assertNotNull(it)

            compareRecipe(recipe, it!!)
        }

    }


    private fun <T> wait(liveData: LiveData<T>, work: (T) -> Unit) {
        val latch = CountDownLatch(1)

        liveData.observeOnce {
            work(it)
            latch.countDown()
        }

        latch.await(10, TimeUnit.SECONDS)
        assertTrue(latch.count == 0L)
    }
}

const val BASE_64_RECIPE = """
    UEsDBBQACAgIAL1wCU8AAAAAAAAAAAAAAAAAAAAA7VfNjhtFEM7uOqvEBzB7QisilYKEkGXNTmyB
FKQoCyI5JQgwPycUlXvK4wo93bP94xx5BB4CLlx4HV6DR6C6Z8b27hsg2VqtxjXVVV9939clefzR
K2tqWAZq4XtS3BK8tA5+IB/Y1Be/bEJo/RdXV9zUxSau1xIM1rTWh0LZ5gq9p3D1mXo6X5XlfD4v
5bNYlOWT1WIxr4q3LdXPbeufeYWa1hzefL4o3xgb2xz48PTxeHyeez+ZnOye55N9fDE5e/z3o/Ff
j15ZRw1w62MDldWC0XMAbCjMQFnjSQUK0QFW3LJXghNIcyhgSRWgCQS2Yiu50fnowZCCBkNgDyte
kaliMwNGFbVEhsoQncECvsEGVhpNJWEhgFdyvoOAClqNihxKoy/JEJr9Yc111AhekSbH/ibSDFrr
goTSJGYGW3ScsPBqI12ilmwSihWnQba0YZUKpNdAEVrSmmSQVKmAr20aYUsapLYUiTo4VuRnsHYy
PKdi1ikWjNLI6hhaHAZaRt/KyCzi7dtsBb7BREiDtcEZ3ER5JvnX2KqLgbeVyOYFfAE/hsPxoY0u
+julh646a0e1kJKVWKNi4VnKtxt0FByCZKV51qKZVPkuZr6SsFvUSRITTZ6WRW5xpuIqmiCoBE3E
CkGThURgo6xrSZQJPaMNxABBqBOOrEzvRAUXgxMbZdyeKpEl6i3Lu65DAd9u0HdHZDxRHKVC08ZE
TRS6i/H4RWApLfcgkiMgw/18VQpjVMG6WVZnLV+yY7IYO3nFLppvIvYoBqoyrbXDLVc4oM687DTt
TCv6J3Uw1ZCu0k3+OgQDpgM2ukuQ33YDQLKn3BipfqB//7Cnlw0kiIM3e/VnnYjial6L1Jkaab43
oBFpoYq8N0fvqH3l3eG30Qebk7HnIxUv4DWSEjsmA4Lson5CUebgEuzMmW9IxbU09iJEtmKvmIjA
AW+x0b9vrM5e+4m32Mj3PcVClA+ptbPCvjW9X/Y3HGvOm0P66ZzQiolZHDVwcIB/t28SYDFbZ7E9
NcP2EU8dV9xxxR1X3HHFHVfcccUdV9xxxR1X3P9xxU1G048f3us+1xejF3XtL9+rHL7DlSb5JU11
DSfl2fSTLun6t+uL+y+1je5ycpC1ThE4Lc+HvHsseT9joNt571IEzsqT6acP/2yfjf/54/T64vyr
GFLiBweJqxyCUflgWvYV/31+MVqiDpfvH+R5CcBoevD7uxxN5z3W3wXDMtZ4G4NPkTtn7g9d0pnR
a9a/3urSSODOidP/AFBLBwhnfqUhuAMAAIsQAABQSwECFAAUAAgICAC9cAlPZ36lIbgDAACLEAAA
AAAAAAAAAAAAAAAAAAAAAAAAUEsFBgAAAAABAAEALgAAAOYDAAAAAA
"""