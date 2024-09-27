package zelgius.com.myrecipes.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import zelgius.com.myrecipes.ui.common.LinkableText
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.ui.AppTheme
import zelgius.com.myrecipes.utils.isTwoPanes

@Composable
fun Settings(viewModel: SettingsViewModel = hiltViewModel(), onBack: () -> Unit) {
    val isIAGenerationChecked by viewModel.isIAGenerationChecked.collectAsState(false)
    val isIAGenerationEnabled by viewModel.isIAGenerationEnabled.collectAsState()

    Settings(
        isIAGenerationChecked = isIAGenerationChecked,
        isIAGenerationEnabled = isIAGenerationEnabled,
        onIAGenerationChanged = viewModel::setIsIAGenerationChecked,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Settings(
    isIAGenerationChecked: Boolean = false,
    isIAGenerationEnabled: Boolean = false,
    onIAGenerationChanged: (Boolean) -> Unit = {},
    onBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.settings)) },
                navigationIcon = {

                    if (!isTwoPanes())
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.TwoTone.ArrowBack,
                                contentDescription = ""
                            )
                        }
                }
            )
        },

        ) {
        LazyColumn(modifier = Modifier
            .padding(it)
            .padding(vertical = 8.dp, horizontal = 16.dp)) {
            item {
                IAGenerationSwitch(
                    isIAGenerationChecked,
                    isIAGenerationEnabled,
                    onIAGenerationChanged
                )
            }
        }
    }

}

@Composable
private fun IAGenerationSwitch(
    isIAGenerationChecked: Boolean,
    isIAGenerationEnabled: Boolean,
    onIAGenerationChanged: (Boolean) -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(id = R.string.ia_generation),
                modifier = Modifier
                    .weight(1f)
                    .align(
                        Alignment.CenterVertically
                    )
            )
            Switch(isIAGenerationChecked, onIAGenerationChanged, enabled = isIAGenerationEnabled)
        }
        if (!isIAGenerationEnabled)
            LinkableText(
                modifier = Modifier.fillMaxWidth(),
                id = R.string.ia_generation_disabled,
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.error)
            ) {
                uriHandler.openUri(it)
            }
    }
}

@Preview
@Composable
private fun SettingsPreview() {
    AppTheme {
        Settings(
            isIAGenerationEnabled = false
        )
    }
}

