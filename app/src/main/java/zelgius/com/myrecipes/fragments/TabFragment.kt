package zelgius.com.myrecipes.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
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

    private lateinit var pagerAdapter: SectionsPagerAdapter

    private var vectorAnimation: AnimatedVectorDrawableCompat? = null
    private var selectRecipe = false
    private lateinit var menu: Menu
    private lateinit var selectPathToExport: ActivityResultLauncher<String>

    private lateinit var container: ViewPager2
    private lateinit var tabs: TabLayout
    private lateinit var searchList: RecyclerView
    private lateinit var add: FloatingActionButton
    private lateinit var toolbar: Toolbar


    private val readQrRequest by lazy {
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK)
                manageQr(it.data)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        readQrRequest

        selectPathToExport = registerForActivityResult(ActivityResultContracts.CreateDocument())
        {
            if (it == null) return@registerForActivityResult

            viewModel.exportSelectionToPdf(it)
                .observe(viewLifecycleOwner) { uri ->
                    menu.findItem(R.id.export).actionView = null
                    viewModel.selectRecipe.value = false

                    Snackbar.make(requireView(), R.string.zip_created, Snackbar.LENGTH_LONG)
                        .setAction(R.string.open) {
                            requireContext().startActivity(
                                Intent.createChooser(
                                    Intent(Intent.ACTION_VIEW)
                                        .setDataAndType(uri, "application/zip")
                                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY),
                                    getString(R.string.select_file)
                                )
                            )
                        }
                        .show()
                }

        }

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (viewModel.selectRecipe.value == true) {
                viewModel.selectRecipe.value = false
            } else
                requireActivity().finish()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pagerAdapter = SectionsPagerAdapter(requireActivity())
        navController = findNavController()
        toolbar = view.findViewById(R.id.toolbar)

        container = view.findViewById(R.id.container)
        container.adapter = pagerAdapter

        tabs = view.findViewById(R.id.tabs)

        TabLayoutMediator(tabs, container) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.meal)
                1 -> getString(R.string.dessert)
                2 -> getString(R.string.other)
                else -> throw IllegalStateException("Should not be there")
            }

        }.attach()

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewModel.selectedType = when (tab?.text) {
                    getString(R.string.meal) -> Recipe.Type.MEAL
                    getString(R.string.dessert) -> Recipe.Type.DESSERT
                    getString(R.string.other) -> Recipe.Type.OTHER
                    else -> throw IllegalStateException("Should not be there")
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}

        })

        container.visibility = View.VISIBLE
        tabs.visibility = View.VISIBLE

        searchList = view.findViewById(R.id.searchList)
        searchList.visibility = View.GONE

        vectorAnimation = AnimatedVectorDrawableCompat.create(ctx, R.drawable.av_add_to_add_list)

        add = view.findViewById(R.id.add)
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

        viewModel.searchResult.observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })

        viewModel.selectRecipe.observe(viewLifecycleOwner) {
            selectRecipe = it
            requireActivity().invalidateOptionsMenu()

            if (it) {
                add.hide()
            } else {
                add.show()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        NavigationUI.setupActionBarWithNavController(
            requireActivity() as AppCompatActivity,
            navController
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        this.menu = menu
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(if (selectRecipe) R.menu.menu_main_selected else R.menu.menu_main, menu)

        // Associate searchable configuration with the SearchView
        (menu.findItem(R.id.search)?.actionView as? SearchView)?.apply {
            setOnQueryTextListener(this@TabFragment)
        }

        if (viewModel.pdfProgress.value == true && viewModel.selectRecipe.value == true) {
            menu.findItem(R.id.export)?.apply {
                actionView = ProgressBar(requireContext())
            }
        } else {
            menu.findItem(R.id.export)?.apply {
                actionView = null
            }
        }

        menu.findItem(R.id.search)?.setOnActionExpandListener(this)

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
            }

            R.id.scan -> {
                readQrRequest.launch(Intent(context, VisionBarcodeReaderActivity::class.java))
            }

            R.id.cancel -> viewModel.selectRecipe.value = false

            R.id.export -> {
                menu.findItem(R.id.export).apply {
                    actionView = ProgressBar(requireContext())
                }

                selectPathToExport.launch(
                    "${
                        resources.getQuantityString(
                            R.plurals.selected_recipes,
                            viewModel.selection.size,
                            viewModel.selection.size
                        )
                    }.zip"
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
