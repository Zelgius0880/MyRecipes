package zelgius.com.myrecipes.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.ArrowBack
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.End
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.data.model.SimpleIngredient
import zelgius.com.myrecipes.data.model.asIngredient
import zelgius.com.myrecipes.ui.AppTheme
import zelgius.com.myrecipes.ui.common.recipe.Ingredient
import zelgius.com.myrecipes.ui.ingredients.UpdateIngredient
import zelgius.com.myrecipes.utils.hasNavigationRail
import java.io.OutputStream

@Composable
fun Settings(viewModel: SettingsViewModel = hiltViewModel(), onBack: () -> Unit) {
    val isIAGenerationChecked by viewModel.isIAGenerationChecked.collectAsStateWithLifecycle(false)
    val isIAGenerationEnabled by viewModel.isIAGenerationEnabled.collectAsStateWithLifecycle()
    val ingredients by viewModel.ingredients.collectAsStateWithLifecycle(emptyList())
    var selectedIngredient by remember {
        mutableStateOf<SimpleIngredient?>(null)
    }

    val exportingProgress by viewModel.exportingProgress.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        viewModel.setIsIAGenerationChecked(isGranted)
    }

    Settings(
        isIAGenerationChecked = isIAGenerationChecked,
        isIAGenerationEnabled = isIAGenerationEnabled,
        onIAGenerationChanged = {
            if (it && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            else
                viewModel.setIsIAGenerationChecked(it)
        },
        ingredients = ingredients,
        exportingProgress = exportingProgress,
        onDeleteIngredient = viewModel::deleteIngredient,
        onUpdateIngredient = {
            selectedIngredient = it
        },
        onExportAll = {
            viewModel.exportAllRecipes(it)
        },
        onGenerateNow = {
            viewModel.generateImageNow()
        },
        onBack = onBack
    )

    AnimatedVisibility(selectedIngredient != null) {
        selectedIngredient?.let {
            UpdateIngredient(
                ingredient = it,
                onDismiss = {
                    selectedIngredient = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Settings(
    isIAGenerationChecked: Boolean = false,
    isIAGenerationEnabled: Boolean = false,
    exportingProgress: Float? = 0f,
    onIAGenerationChanged: (Boolean) -> Unit = {},
    ingredients: List<SimpleIngredient> = emptyList(),
    onDeleteIngredient: (SimpleIngredient) -> Unit = {},
    onUpdateIngredient: (SimpleIngredient) -> Unit = {},
    onExportAll: suspend (outputStream: OutputStream) -> Unit = {},
    onGenerateNow: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.settings)) },
                navigationIcon = {

                    if (!hasNavigationRail())
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
        LazyColumn(
            modifier = Modifier
                .padding(it)
                .padding(vertical = 8.dp),
        ) {
            item {
                ExportButton(onExportAll, exportingProgress)
            }

            item {
                IAGenerationSwitch(
                    isIAGenerationChecked,
                    isIAGenerationEnabled,
                    onIAGenerationChanged,
                    onGenerateNow
                )
            }

            item {
                Text(
                    text = stringResource(id = R.string.ingredients),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp)
                )
            }

            itemsIndexed(ingredients, key = { _, item -> item.id }) { index, item ->
                Column(
                    modifier = Modifier
                        .animateItem()
                        .clickable { onUpdateIngredient(item) }
                        .padding(top = 8.dp),
                ) {
                    SettingsIngredient(ingredient = item, onDeleteIngredient = onDeleteIngredient)

                    if (index < ingredients.lastIndex)
                        HorizontalDivider(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .padding(horizontal = 16.dp)
                        )
                }
            }
        }
    }

}

@Composable
private fun ExportButton(
    onExportAll: suspend (outputStream: OutputStream) -> Unit,
    exportingProgress: Float?
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val saveFileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) {

            coroutineScope.launch {
                if (it != null) {
                    val outputStream: OutputStream = context.contentResolver?.openOutputStream(it)
                        ?: return@launch
                    onExportAll(outputStream)
                    outputStream.close()

                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(it, "application/zip")
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    context.startActivity(
                        Intent.createChooser(
                            intent,
                            context.getString(R.string.export_all_recipes)
                        )
                    )
                }
            }
        }

    Box(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = {
                if (exportingProgress == null) saveFileLauncher.launch("Export.zip")
            },
            modifier = Modifier
                .padding(end = 16.dp)
                .align(Alignment.Center),
            contentPadding = PaddingValues(
                top = 8.dp,
                bottom = 8.dp,
                start = 24.dp,
                end = if (exportingProgress == null) 24.dp else 8.dp
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(id = R.string.export_all_recipes),
                    modifier = Modifier.padding(end = 8.dp)
                )

                if (exportingProgress != null)
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.CenterVertically),
                        color = MaterialTheme.colorScheme.secondary,
                        progress = { exportingProgress })
            }
        }
    }
}

@Composable
private fun IAGenerationSwitch(
    isIAGenerationChecked: Boolean,
    isIAGenerationEnabled: Boolean,
    onIAGenerationChanged: (Boolean) -> Unit,
    onGenerateNow: () -> Unit = {}
) {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp),
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

        AnimatedVisibility(
            isIAGenerationEnabled && isIAGenerationChecked,
            modifier = Modifier.align(End)
        ) {
            OutlinedButton(onClick = onGenerateNow) {
                Text(stringResource(R.string.generate_now))
            }
        }


        if (!isIAGenerationEnabled){
            // TODO: By feature
        }
    }
}

@Composable
private fun SettingsIngredient(
    ingredient: SimpleIngredient,
    modifier: Modifier = Modifier,
    onDeleteIngredient: (SimpleIngredient) -> Unit = {},
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Ingredient(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
                .heightIn(min = 48.dp),
            ingredient = ingredient.asIngredient(),
            text = ingredient.name
        )

        if (ingredient.removable) {
            IconButton(onClick = { onDeleteIngredient(ingredient) }) {
                Icon(
                    imageVector = Icons.TwoTone.Delete,
                    contentDescription = ""
                )
            }
        }
    }
}

@Preview
@Composable
private fun SettingsPreview() {
    AppTheme {
        Settings(
            isIAGenerationEnabled = true,
            isIAGenerationChecked = true,
            ingredients = List(5) {
                SimpleIngredient(
                    it.toLong(),
                    "Ingredient $it",
                    null,
                    it % 2 == 0
                )
            }
        )
    }
}

