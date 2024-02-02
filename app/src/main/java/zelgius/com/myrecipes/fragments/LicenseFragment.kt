package zelgius.com.myrecipes.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.ui.license.DrawLicense
import zelgius.com.myrecipes.ui.darkThemeColors
import zelgius.com.myrecipes.ui.lightThemeColors

/**
 * A simple [Fragment] subclass.
 *
 */
class LicenseFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return (inflater.inflate(
            R.layout.layout_compose_view,
            container,
            false
        ) as ComposeView).apply {
            setContent {
                MaterialTheme(
                    colors = if (isSystemInDarkTheme()) {
                        darkThemeColors
                    } else {
                        lightThemeColors
                    }
                ) {
                    DrawLicense()
                }
            }
        }
    }


}
