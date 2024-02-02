package zelgius.com.myrecipes.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.fragment.app.DialogFragment
import zelgius.com.myrecipes.BuildConfig
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.ui.license.EmailText
import zelgius.com.myrecipes.ui.darkThemeColors
import zelgius.com.myrecipes.ui.lightThemeColors

class IntroDialog : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppTheme_Dialog_Alert_Title)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = getString(R.string.intro_text),
                            color = MaterialTheme.colors.onBackground
                        )
                        EmailText(text = BuildConfig.EMAIL, url = BuildConfig.EMAIL)
                        Button(onClick = { dismiss() }, modifier = Modifier.align(Alignment.End)) {
                            Text(text = getString(android.R.string.ok))
                        }
                    }
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setTitle(R.string.welcome)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        requireContext().getSharedPreferences("DEFAULT", AppCompatActivity.MODE_PRIVATE).edit {
            putBoolean("SHOW_POP", false)
        }
    }
}