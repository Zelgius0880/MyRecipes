package zelgius.com.myrecipes.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import kotlinx.android.synthetic.main.activity_recipe.*
import kotlinx.android.synthetic.main.fragment_recipe.*
import kotlinx.android.synthetic.main.fragment_tab.*
import zelgius.com.myrecipes.utils.AnimationUtils
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.RecipeActivity
import zelgius.com.myrecipes.utils.colorSecondary
import zelgius.com.myrecipes.entities.Recipe
import kotlin.math.roundToInt

class TabFragment : Fragment() {

    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_tab, container, false)

    private var vectorAnimation: AnimatedVectorDrawableCompat? = null

    private val ctx by lazy { activity!! }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()

        //add.setImageResource(R.drawable.ic_add_24dp)

        container.adapter = SectionsPagerAdapter(fragmentManager!!)

        vectorAnimation = AnimatedVectorDrawableCompat.create(ctx, R.drawable.av_add_to_add_list)

        add.setOnClickListener {
            add.setImageDrawable(vectorAnimation)
            vectorAnimation?.start()
            /*navController.navigate(
                R.id.action_tabFragment_to_recipeActivity,
                bundleOf( "EXTRA_CIRCULAR_REVEAL_SETTINGS" to
                    AnimationUtils.RevealAnimationSetting(
                        (add.x + add.width / 2).roundToInt(),
                        (add.y + add.height / 2).roundToInt(),
                        view.width,
                        view.height
                    ),
                    "ADD" to true
                )
            )*/

            startActivityForResult(Intent(ctx, RecipeActivity::class.java).apply {
                putExtra(
                    AnimationUtils.EXTRA_CIRCULAR_REVEAL_SETTINGS,
                    AnimationUtils.RevealAnimationSetting(
                        (add.x + add.width / 2).roundToInt(),
                        (add.y + add.height / 2).roundToInt(),
                        view.width,
                        view.height
                    )
                )
            }, RecipeActivity.REQUEST_CODE)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            RecipeActivity.REQUEST_CODE -> {
                val vectorAnimation = AnimatedVectorDrawableCompat.create(ctx, R.drawable.av_add_list_to_add)

                add.setImageDrawable(vectorAnimation)
                vectorAnimation?.start()
            }
        }
    }


    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: androidx.fragment.app.FragmentManager) :
        androidx.fragment.app.FragmentPagerAdapter(fm) {

        private val fragments = listOf(
            ListFragment.newInstance(Recipe.Type.MEAL),
            ListFragment.newInstance(Recipe.Type.DESSERT),
            ListFragment.newInstance(Recipe.Type.OTHER)
        )

        override fun getItem(position: Int): androidx.fragment.app.Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return fragments[position]
        }

        override fun getCount(): Int {
            // Show 3 total pages.
            return fragments.size
        }
    }

}
