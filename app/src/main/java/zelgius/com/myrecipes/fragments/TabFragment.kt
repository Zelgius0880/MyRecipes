package zelgius.com.myrecipes.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_tab.*
import kotlinx.android.synthetic.main.fragment_tab.view.*
import zelgius.com.myrecipes.MainActivity
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.VisionBarcodeReaderActivity
import zelgius.com.myrecipes.entities.Recipe
import zelgius.com.myrecipes.utils.AnimationUtils
import zelgius.com.myrecipes.utils.observe
import kotlin.math.roundToInt


class TabFragment : AbstractRecipeListFragment(), SearchView.OnQueryTextListener,
    MenuItem.OnActionExpandListener {


    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_tab, container, false).also {
        setHasOptionsMenu(true)
        recyclerView = it.findViewById(R.id.searchList)
    }

    private val pagerAdapter by lazy { SectionsPagerAdapter(childFragmentManager) }

    private var vectorAnimation: AnimatedVectorDrawableCompat? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()

        //add.setImageResource(R.drawable.ic_add_24dp)

        container.adapter = pagerAdapter
        tabs.setupWithViewPager(container)
        //container.isSaveFromParentEnabled = true

        container.visibility = View.VISIBLE
        tabs.visibility = View.VISIBLE
        searchList.visibility = View.GONE

        vectorAnimation = AnimatedVectorDrawableCompat.create(ctx, R.drawable.av_add_to_add_list)

        add.setOnClickListener {
            add.setImageDrawable(vectorAnimation)
            vectorAnimation?.start()
            viewModel.selectedImageUrl.value = null

            navController.navigate(
                R.id.action_tabFragment_to_editRecipeFragment, bundleOf(
                    "ADD" to true,
                    AnimationUtils.EXTRA_CIRCULAR_REVEAL_SETTINGS to
                            AnimationUtils.RevealAnimationSetting(
                                (add.x + add.width / 2).roundToInt(),
                                (add.y + add.height / 2).roundToInt(),
                                view.width,
                                view.height
                            )
                )
            )
        }

        viewModel.searchResult.observe(this, Observer {
            adapter.submitList(it)
        })
    }

    override fun onResume() {
        super.onResume()

        (activity as AppCompatActivity).setSupportActionBar(view!!.toolbar)
        NavigationUI.setupActionBarWithNavController(activity!! as AppCompatActivity, navController)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu)

        // Associate searchable configuration with the SearchView
        (menu.findItem(R.id.search).actionView as SearchView).apply {
            setOnQueryTextListener(this@TabFragment)
        }

        menu.findItem(R.id.search).setOnActionExpandListener(this)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        when (id) {
            R.id.action_license -> {
                navController.navigate(
                    R.id.licenseFragment, bundleOf(), NavOptions.Builder()
                        .setEnterAnim(R.anim.nav_default_enter_anim)
                        .setExitAnim(R.anim.nav_default_exit_anim)
                        .setPopEnterAnim(R.anim.nav_default_pop_enter_anim)
                        .setPopExitAnim(R.anim.nav_default_pop_exit_anim)
                        .build()
                )
                return true
            }

            R.id.scan -> {

                startActivityForResult(
                    Intent(context, VisionBarcodeReaderActivity::class.java),
                    888
                )
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onQueryTextSubmit(s: String?): Boolean {
        viewModel.search(s ?: "")
        return true
    }

    override fun onQueryTextChange(s: String?): Boolean {
        if (s != null && s.length > 3)
            viewModel.search(s)

        return s != null && s.length > 3
    }

    override fun onMenuItemActionExpand(p0: MenuItem?): Boolean {
        container.visibility = View.GONE
        tabs.visibility = View.GONE
        searchList.visibility = View.VISIBLE
        return true
    }


    override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
        container.visibility = View.VISIBLE
        tabs.visibility = View.VISIBLE
        searchList.visibility = View.GONE
        return true
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            888 -> {
                if (resultCode == RESULT_OK && data?.hasExtra("BASE64") == true) {
                    viewModel.saveFromQrCode(data.getStringExtra("BASE64")!!)
                        .observe(this) {
                            if (it == null) {
                                Snackbar.make(
                                    (activity as MainActivity).coordinator,
                                    R.string.import_failed,
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            } else {
                                Snackbar.make(
                                    (activity as MainActivity).coordinator,
                                    R.string.recipe_saved,
                                    Snackbar.LENGTH_SHORT
                                ).setAction(R.string.open) { _ ->
                                    Navigation.findNavController(requireView()).navigate(
                                        R.id.action_tabFragment_to_recipeFragment
                                        , bundleOf("RECIPE" to it), null, null
                                    )

                                    viewModel.loadRecipe(it.id!!)
                                }.show()

                            }
                        }
                }
            }
        }
    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) :
        FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private val fragments = listOf(
            ListFragment.newInstance(Recipe.Type.MEAL),
            ListFragment.newInstance(Recipe.Type.DESSERT),
            ListFragment.newInstance(Recipe.Type.OTHER)
        )

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return fragments[position]
        }

        override fun getPageTitle(position: Int): CharSequence? =
            when (position) {
                0 -> getString(R.string.meal)
                1 -> getString(R.string.dessert)
                2 -> getString(R.string.other)
                else -> throw IllegalStateException("Should not be there")
            }

        override fun getCount(): Int {
            // Show 3 total pages.
            return fragments.size
        }
    }

}
