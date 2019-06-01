package zelgius.com.myrecipes

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import kotlinx.android.synthetic.main.activity_recipe.*
import kotlinx.android.synthetic.main.fragment_tab.view.*
import net.alhazmy13.mediapicker.Image.ImagePicker
import zelgius.com.myrecipes.adapters.RecipeEditAdapter
import zelgius.com.myrecipes.dialogs.IngredientDialogFragment
import zelgius.com.myrecipes.dialogs.StepDialogFragment
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
    private val adapter by lazy { RecipeEditAdapter(this, viewModel) }


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

        //toolbar.title = getString(R.string.new_recipe)
        toolbar.setNavigationOnClickListener {
            val upIntent: Intent? = NavUtils.getParentActivityIntent(this)

            /*when {
                upIntent == null -> throw IllegalStateException("No Parent Activity Intent")
                NavUtils.shouldUpRecreateTask(this, upIntent) -> {
                    TaskStackBuilder.create(this)
                        .addNextIntentWithParentStack(upIntent)
                        .startActivities()
                }
                else -> {
                    overridePendingTransition(R.anim.do_not_move, R.anim.do_not_move)
                    NavUtils.navigateUpTo(this, upIntent)
                    overridePendingTransition(R.anim.do_not_move, R.anim.do_not_move)
                    endActivity()

                }
            }*/

            endActivity()
        }

        viewModel.selectedRecipe.value = intent.getParcelableExtra("RECIPE") ?: Recipe(Recipe.Type.MEAL)
        viewModel.editMode.value = true

        fab.setImageResource(R.drawable.ic_playlist_plus)

        viewModel.editMode.observe(this, Observer {
            adapter.notifyDataSetChanged()
        })

        viewModel.selectedRecipe.observe(this, Observer {
            viewModel.currentRecipe = it
            adapter.recipe = it
            adapter.notifyDataSetChanged()
        })

        list.adapter = adapter
        list.addItemDecoration(
            adapter.DividerDecoration(
                this, ContextCompat.getColor(this, android.R.color.transparent), 8f
            )
        )
        ItemTouchHelper(adapter.TouchHelper()).attachToRecyclerView(list)

        adapter.editIngredientListener = {
            IngredientDialogFragment.newInstance(it)
                .show(supportFragmentManager, "dialog_ingredient")
        }

        adapter.editStepListener = {
            StepDialogFragment.newInstance(it)
                .show(supportFragmentManager, "dialog_step")
        }

        fab.menuLayouts = arrayOf(addStepLayout, addIngredientLayout)
        fab.animation = AnimatedVectorDrawableCompat.create(this, R.drawable.av_add_list_to_close)!! to
                AnimatedVectorDrawableCompat.create(this, R.drawable.av_close_to_add_list)!!

        addIngredient.setOnClickListener {
            IngredientDialogFragment().show(supportFragmentManager, "dialog_ingredient")
        }

        addStep.setOnClickListener {
            StepDialogFragment().show(supportFragmentManager, "dialog_step")
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

        if (data != null && requestCode == ImagePicker.IMAGE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val paths = data.getStringArrayListExtra(ImagePicker.EXTRA_IMAGE_PATH)!!

            if (paths.isNotEmpty()) {
                viewModel.selectedImageUrl.value = Uri.parse("file://${paths.first()}")
            }
        }
    }

    private fun endActivity(resultCode: Int = Activity.RESULT_CANCELED) {
        with(intent.getParcelableExtra<AnimationUtils.RevealAnimationSetting>("EXTRA_CIRCULAR_REVEAL_SETTINGS")) {
            if (this != null) {
                /*fab.setImageDrawable(vectorAnimation)
                vectorAnimation?.start()*/

                AnimationUtils.exitCircularRevealAnimation(
                    this@RecipeActivity,
                    rootLayout,
                    this,
                    this@RecipeActivity.colorSecondary,
                    Color.WHITE
                ) {
                    setResult(resultCode)
                    finish()
                }
            }
        }
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        when (dialog.tag) {
            "dialog_ingredient" -> {
                if (dialog is IngredientDialogFragment) {
                    dialog.ingredient.let {
                        if (it.new) {
                            viewModel.currentRecipe.ingredients.add(it)
                            it.sortOrder = viewModel.currentRecipe.ingredients.size
                            it.new = false

                            adapter.addIngredient(it)
                        } else {
                            adapter.notifyIngredientChanged(it)
                        }
                    }
                }
            }

            "dialog_step" -> {
                if (dialog is StepDialogFragment) {
                    dialog.step.let {
                        if (it.new) {
                            viewModel.currentRecipe.steps.add(it)
                            it.order = viewModel.currentRecipe.steps.size
                            it.new = false

                            adapter.addStep(it)
                        } else {
                            adapter.notifyStepChanged(it)
                        }
                    }
                }
            }
        }
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
    }
}
