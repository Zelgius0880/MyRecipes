package zelgius.com.myrecipes

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation.findNavController
import com.facebook.stetho.Stetho
import net.alhazmy13.mediapicker.Image.ImagePicker
import zelgius.com.myrecipes.fragments.OnBackPressedListener
import zelgius.com.myrecipes.utils.observe


class MainActivity : AppCompatActivity() {


    private val navController by lazy { findNavController(this, R.id.nav_host_fragment) }

    private val viewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelProvider.NewInstanceFactory()
        ).get(RecipeViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Stetho.initializeWithDefaults(this)
        if (intent != null) processIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent != null) processIntent(intent)
    }

    private fun processIntent(intent: Intent) {
        if (intent.hasExtra("ID_FROM_NOTIF")) {
            viewModel.loadRecipe(intent.getLongExtra("ID_FROM_NOTIF", 0L)).observe(this) {
                if (it != null) {
                    if (navController.currentDestination?.id != R.id.tabFragment)
                        navController.navigate(R.id.tabFragment)

                    navController.navigate(
                        R.id.action_tabFragment_to_recipeFragment,
                        bundleOf("RECIPE" to it),
                        null,
                        null
                    )

                    viewModel.loadRecipe(it.id!!)
                }
            }

        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val fragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            ?.childFragmentManager
            ?.fragments?.last()

        if (fragment is OnBackPressedListener) {
            fragment.onBackPressed()
        }

        return navController.navigateUp()
    }


    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            ?.childFragmentManager
            ?.fragments?.last()

        if (fragment is OnBackPressedListener) {
            fragment.onBackPressed()
        }

        super.onBackPressed()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (resultCode == Activity.RESULT_OK)
            when (requestCode) {
                ImagePicker.IMAGE_PICKER_REQUEST_CODE -> {
                    val paths = data?.getStringArrayListExtra(ImagePicker.EXTRA_IMAGE_PATH)

                    if (!paths.isNullOrEmpty()) {
                        viewModel.selectedImageUrl.value = Uri.parse("file://${paths.first()}")
                    }
                }

            }
    }
}