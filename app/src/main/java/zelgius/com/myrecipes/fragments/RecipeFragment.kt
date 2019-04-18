package zelgius.com.myrecipes.fragments


import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import kotlinx.android.synthetic.main.fragment_recipe.*
import kotlinx.android.synthetic.main.fragment_recipe.view.*
import zelgius.com.myrecipes.utils.AnimationUtils
import zelgius.com.myrecipes.utils.AnimationUtils.RevealAnimationSetting
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.RecipeViewModel
import zelgius.com.myrecipes.adapters.RecipeDetailsAdapter
import zelgius.com.myrecipes.utils.colorSecondary


/**
 * A simple [Fragment] subclass.
 *
 */
class RecipeFragment : Fragment(), OnBackPressedListener {

    override fun onBackPressed() {
        with(view) {
            if (this != null && arguments != null
                && arguments?.containsKey("EXTRA_CIRCULAR_REVEAL_SETTINGS") == true
            ) {
                fab.setImageDrawable(vectorAnimation)
                vectorAnimation?.start()

                AnimationUtils.exitCircularRevealAnimation(
                    context,
                    this,
                    arguments!!.getParcelable("EXTRA_CIRCULAR_REVEAL_SETTINGS")!!,
                    context.colorSecondary,
                    Color.WHITE
                )
            }
        }
    }

    private var vectorAnimation: AnimatedVectorDrawableCompat? = null

    val context by lazy { activity!! }
    private val viewModel by lazy { ViewModelProviders.of(this).get(RecipeViewModel::class.java) }
    private val adapter by lazy { RecipeDetailsAdapter(context, viewModel) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vectorAnimation = AnimatedVectorDrawableCompat.create(activity!!, R.drawable.av_add_list_to_add)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_recipe, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (arguments != null && arguments?.containsKey("EXTRA_CIRCULAR_REVEAL_SETTINGS") == true) {
            AnimationUtils.enterCircularRevealAnimation(
                context,
                view,
                arguments!!.getParcelable("EXTRA_CIRCULAR_REVEAL_SETTINGS")!!,
                context.colorSecondary,
                Color.WHITE
            )
        }


        fab.setImageResource(R.drawable.ic_playlist_plus)

        viewModel.editMode.observe(this, Observer {
            adapter.edit = it
            adapter.notifyDataSetChanged()
        })

        viewModel.selectedRecipe.observe(this, Observer {
            adapter.recipe = it
            adapter.notifyDataSetChanged()
        })

        view.list.adapter = adapter

        view.fab.menuLayouts = arrayOf(view.addStepLayout, view.addIngredientLayout)
        view.fab.animation = AnimatedVectorDrawableCompat.create(activity!!, R.drawable.av_add_list_to_close)!! to
                AnimatedVectorDrawableCompat.create(activity!!, R.drawable.av_close_to_add_list)!!
    }



}
