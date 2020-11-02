package zelgius.com.myrecipes.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_tab.view.*
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.VisionBarcodeReaderActivity
import zelgius.com.myrecipes.entities.Recipe
import zelgius.com.myrecipes.utils.AnimationUtils
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

    private val pagerAdapter by lazy { SectionsPagerAdapter(requireActivity()) }

    private var vectorAnimation: AnimatedVectorDrawableCompat? = null

    private val readQrRequest by lazy {
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK)
                manageQr(it.data)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        readQrRequest
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()

        view.container.adapter = pagerAdapter

        TabLayoutMediator(view.tabs, view.container) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.meal)
                1 -> getString(R.string.dessert)
                2 -> getString(R.string.other)
                else -> throw IllegalStateException("Should not be there")
            }
        }

        view.container.visibility = View.VISIBLE
        view.tabs.visibility = View.VISIBLE
        view.searchList.visibility = View.GONE

        vectorAnimation = AnimatedVectorDrawableCompat.create(ctx, R.drawable.av_add_to_add_list)

        view.add.setOnClickListener {
            view.add.setImageDrawable(vectorAnimation)
            vectorAnimation?.start()
            viewModel.selectedImageUrl.value = null

            navController.navigate(
                R.id.action_tabFragment_to_editRecipeFragment, bundleOf(
                    "ADD" to true,
                    AnimationUtils.EXTRA_CIRCULAR_REVEAL_SETTINGS to
                            AnimationUtils.RevealAnimationSetting(
                                (view.add.x + view.add.width / 2).roundToInt(),
                                (view.add.y + view.add.height / 2).roundToInt(),
                                view.width,
                                view.height
                            )
                )
            )
        }

        viewModel.searchResult.observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })
    }

    override fun onResume() {
        super.onResume()

        (activity as AppCompatActivity).setSupportActionBar(requireView().toolbar)
        NavigationUI.setupActionBarWithNavController(
            requireActivity() as AppCompatActivity,
            navController
        )
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

        when (item.itemId) {
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
                readQrRequest.launch(Intent(context, VisionBarcodeReaderActivity::class.java))
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
        requireView().container.visibility = View.GONE
        requireView().tabs.visibility = View.GONE
        requireView().searchList.visibility = View.VISIBLE
        return true
    }


    override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
        requireView().container.visibility = View.VISIBLE
        requireView().tabs.visibility = View.VISIBLE
        requireView().searchList.visibility = View.GONE
        return true
    }

    private fun manageQr(data: Intent?) {
        if (data?.hasExtra("BASE64") == true) {
            viewModel.saveFromQrCode(data.getStringExtra("BASE64")!!)
                .observe(this) {
                    if (it == null) {
                        Snackbar.make(
                            requireView(),
                            R.string.import_failed,
                            Snackbar.LENGTH_SHORT
                        ).show()
                    } else {
                        Snackbar.make(
                            requireView(),
                            R.string.recipe_saved,
                            Snackbar.LENGTH_SHORT
                        ).setAction(R.string.open) { _ ->
                            Navigation.findNavController(requireView()).navigate(
                                R.id.action_tabFragment_to_recipeFragment,
                                bundleOf("RECIPE" to it),
                                null,
                                null
                            )

                            viewModel.loadRecipe(it.id!!)
                        }.show()
                    }
                }
        }
    }

    inner class SectionsPagerAdapter(activity: FragmentActivity) :
        FragmentStateAdapter(activity) {

        private val fragments = listOf(
            ListFragment.newInstance(Recipe.Type.MEAL),
            ListFragment.newInstance(Recipe.Type.DESSERT),
            ListFragment.newInstance(Recipe.Type.OTHER)
        )

        override fun getItemCount(): Int = fragments.size

        override fun createFragment(position: Int): Fragment = fragments[position]
    }

}
