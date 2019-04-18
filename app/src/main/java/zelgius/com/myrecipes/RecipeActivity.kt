package zelgius.com.myrecipes

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import kotlinx.android.synthetic.main.activity_recipe.*
import net.alhazmy13.mediapicker.Image.ImagePicker
import zelgius.com.myrecipes.adapters.RecipeDetailsAdapter
import zelgius.com.myrecipes.dialogs.IngredientDialogFragment
import zelgius.com.myrecipes.entities.Recipe
import zelgius.com.myrecipes.utils.AnimationUtils
import zelgius.com.myrecipes.utils.AnimationUtils.EXTRA_CIRCULAR_REVEAL_SETTINGS
import zelgius.com.myrecipes.utils.colorSecondary


class RecipeActivity : AppCompatActivity(), NoticeDialogListener {

    companion object {
        const val REQUEST_CODE = 543
    }

    override fun onBackPressed() {
        endActivity()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.do_not_move, R.anim.do_not_move)
    }

    private var vectorAnimation: AnimatedVectorDrawableCompat? = null

    private val viewModel by lazy { ViewModelProviders.of(this).get(RecipeViewModel::class.java) }
    private val adapter by lazy { RecipeDetailsAdapter(this, viewModel) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        overridePendingTransition(R.anim.do_not_move, R.anim.do_not_move)

        fab.menuLayouts = arrayOf(addStepLayout, addIngredientLayout)
        fab.animation = AnimatedVectorDrawableCompat.create(this, R.drawable.av_add_list_to_close)!! to
                AnimatedVectorDrawableCompat.create(this, R.drawable.av_close_to_add_list)!!

        vectorAnimation = AnimatedVectorDrawableCompat.create(this, R.drawable.av_add_list_to_add)

        if (intent.hasExtra(EXTRA_CIRCULAR_REVEAL_SETTINGS)) {

            AnimationUtils.enterCircularRevealAnimation(
                this,
                rootLayout,
                intent.getParcelableExtra(EXTRA_CIRCULAR_REVEAL_SETTINGS)!!,
                colorSecondary,
                Color.WHITE
            )
        }

        viewModel.selectedRecipe.value = intent.getParcelableExtra("RECIPE") ?: Recipe(Recipe.Type.MEAL)
        viewModel.editMode.value = true

        fab.setImageResource(R.drawable.ic_playlist_plus)

        viewModel.editMode.observe(this, Observer {
            adapter.edit = it
            adapter.notifyDataSetChanged()
        })

        viewModel.selectedRecipe.observe(this, Observer {
            adapter.recipe = it
            adapter.notifyDataSetChanged()
        })

        list.adapter = adapter

        fab.menuLayouts = arrayOf(addStepLayout, addIngredientLayout)
        fab.animation = AnimatedVectorDrawableCompat.create(this, R.drawable.av_add_list_to_close)!! to
                AnimatedVectorDrawableCompat.create(this, R.drawable.av_close_to_add_list)!!

        addIngredient.setOnClickListener {
            IngredientDialogFragment().show(supportFragmentManager, "dialog_ingredient")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_recipe, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                endActivity()

                true
            }
            else -> super.onOptionsItemSelected(item)

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data!= null && requestCode == ImagePicker.IMAGE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val paths = data.getStringArrayListExtra(ImagePicker.EXTRA_IMAGE_PATH)!!

            if(paths.isNotEmpty()){
                viewModel.selectedImageUrl.value = Uri.parse("file://${paths.first()}")
            }
        }
    }

    private fun endActivity(resultCode: Int = Activity.RESULT_CANCELED){
        with(intent.getParcelableExtra<AnimationUtils.RevealAnimationSetting>("EXTRA_CIRCULAR_REVEAL_SETTINGS")) {
            if (this != null) {
                /*fab.setImageDrawable(vectorAnimation)
                vectorAnimation?.start()*/

                AnimationUtils.exitCircularRevealAnimation(
                    this@RecipeActivity,
                    rootLayout,
                    this,
                    this@RecipeActivity.colorSecondary,
                    android.graphics.Color.WHITE
                ){
                    setResult(resultCode)
                    finish()
                }
            }
        }
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {

    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
    }
}
