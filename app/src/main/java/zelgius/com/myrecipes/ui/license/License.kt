package zelgius.com.myrecipes.ui.license

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import zelgius.com.myrecipes.BuildConfig
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.ui.darkThemeColors
import zelgius.com.myrecipes.ui.lightThemeColors
import java.util.Locale

@Composable
fun LicenceDialog(onDismiss: () -> Unit = {}) {
    val list = remember {
        icons.split("\n").map { it.split(";") }
            .filter { it.size > 1 && it[0].isNotBlank() }
    }

    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(modifier = Modifier.padding(16.dp).height(500.dp)) {
            LazyColumn {
                item {
                    Column(modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)) {
                        Text(
                            text = stringResource(R.string.intro_text_license)
                        )
                        EmailText(BuildConfig.EMAIL, BuildConfig.EMAIL)
                    }
                }

                item {
                    TextButton(
                        modifier = Modifier
                            .fillMaxWidth(),
                        onClick = {
                            context.startActivity(
                                Intent(
                                    context,
                                    OssLicensesMenuActivity::class.java
                                )
                            )
                        }) {
                        Text(
                            stringResource(R.string.opensource_licenses),
                            style = TextStyle(color = MaterialTheme.colorScheme.secondary)
                        )
                    }
                }

                items(list) {
                    IconLicense(
                        s = it, modifier = Modifier
                            .padding(vertical = 16.dp, horizontal = 4.dp)
                            .fillMaxWidth()
                    )
                }

            }
        }
    }
}


@Composable
fun IconLicense(s: List<String>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row {
            Image(
                contentDescription = "",
                modifier = Modifier.size(24.dp),
                painter = painterResource(
                    id =
                        when (s[0].uppercase(Locale.getDefault())) {
                            "EGG" -> R.drawable.ic_eggs
                            "FLOUR" -> R.drawable.ic_flour
                            "SUGAR" -> R.drawable.ic_suggar
                            "WATER" -> R.drawable.ic_drop
                            "MILK" -> R.drawable.ic_milk
                            "BUTTER" -> R.drawable.ic_butter
                            "SALT" -> R.drawable.ic_salt
                            "APPLE" -> R.drawable.ic_apple
                            "AVOCADO" -> R.drawable.ic_avocado
                            "BROWN_SUGAR" -> R.drawable.ic_brown_suggar
                            "CHEESE" -> R.drawable.ic_cheese
                            "CHOCOLATE" -> R.drawable.ic_chocolate
                            "COCONUT" -> R.drawable.ic_coconut
                            "COFFEE" -> R.drawable.ic_coffee
                            "HAZELNUT" -> R.drawable.ic_hazelnut
                            "HONEY" -> R.drawable.ic_honey
                            "NUTS" -> R.drawable.ic_nuts
                            "PEANUT" -> R.drawable.ic_peanut
                            "PEAR" -> R.drawable.ic_pear
                            "PEPPER" -> R.drawable.ic_pepper
                            "POTATO" -> R.drawable.ic_potatoes
                            "RASPBERRY" -> R.drawable.ic_raspberry
                            "STRAWBERRY" -> R.drawable.ic_strawberry
                            "TOMATO" -> R.drawable.ic_tomato
                            "TOMATO SAUCE" -> R.drawable.ic_tomato_sauce
                            "QRCODE" -> R.drawable.ic_qr_code
                            "DISH" -> R.drawable.ic_dish
                            "STEP" -> R.drawable.ic_shoe_prints_solid
                            "CARROT" -> R.drawable.ic_carrot_solid
                            else -> error("Unknown image: ${s[0]}")
                        }
                )
            )

            if (s[1].isNotBlank()) {
                Text(s[1])
                UrlText(text = s[3], url = s[2])
            }
        }
        Row {
            Text(text = s[4])
            UrlText(text = s[6], url = s[5])
        }

        Row {
            Text(text = s[7])
            UrlText(text = s[9], url = s[8])
        }
    }
}

@Composable
fun UrlText(text: String, url: String, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }

    val annotatedString = AnnotatedString.Builder().apply {
        pushStyle(
            SpanStyle(
                color = MaterialTheme.colorScheme.secondary,
                textDecoration = TextDecoration.Underline
            )
        )
        addStringAnnotation("url", url, 0, url.length)
        append(text)
    }.toAnnotatedString()

    Text(
        modifier = modifier then Modifier.pointerInput(Unit) {
            detectTapGestures { pos ->
                layoutResult.value?.let { layoutResult ->
                    val position = layoutResult.getOffsetForPosition(pos)
                    annotatedString.getStringAnnotations(position, position)
                        .firstOrNull()
                        ?.let { sa ->
                            uriHandler.openUri(sa.item)
                        }
                }
            }
        },
        text = annotatedString,
        onTextLayout = { layoutResult.value = it }
    )
}

@Composable
fun EmailText(text: String, url: String, modifier: Modifier = Modifier) {
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }

    val annotatedString = AnnotatedString.Builder().apply {
        pushStyle(
            SpanStyle(
                color = MaterialTheme.colorScheme.secondary,
                textDecoration = TextDecoration.Underline
            )
        )
        addStringAnnotation("email", url, 0, url.length)
        append(text)
    }.toAnnotatedString()

    val context = LocalContext.current
    Text(
        modifier = modifier then Modifier.pointerInput(Unit) {
            detectTapGestures { pos ->
                layoutResult.value?.let { layoutResult ->
                    val position = layoutResult.getOffsetForPosition(pos)
                    annotatedString.getStringAnnotations(position, position)
                        .firstOrNull()
                        ?.let { _ ->
                            context.startActivity(
                                Intent.createChooser(
                                    Intent(Intent.ACTION_SENDTO).apply {
                                        data = "mailto:${BuildConfig.EMAIL}".toUri()
                                    },
                                    context.getString(R.string.send_email)
                                )
                            )
                        }
                }
            }
        },
        text = annotatedString,
        onTextLayout = { layoutResult.value = it }
    )
}

@Composable
@Preview
fun LicensePreview() {

    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) {
            darkThemeColors
        } else {
            lightThemeColors
        }
    ) {
        LicenceDialog()
    }
}

const val icons =
    """
EGG; made by ;https://www.flaticon.com/authors/smash ;Smash; from ;https://www.flaticon.com/ ;www.flaticon.com; is licensed by ;http://creativecommons.org/licenses/by/3.0/ ;Creative Commons BY 3.0
FLOUR; made by ;https://www.flaticon.com/authors/monkik ;monkik; from ;https://www.flaticon.com/ ;www.flaticon.com; is licensed by ;http://creativecommons.org/licenses/by/3.0/ ;Creative Commons BY 3.0
Butter; made by ;https://www.freepik.com/ ;Freepik; from ;https://www.flaticon.com/ ;www.flaticon.com; is licensed by ;http://creativecommons.org/licenses/by/3.0/ ;Creative Commons BY 3.0
Milk; made by ;https://www.freepik.com/ ;Freepik; from ;https://www.flaticon.com/ ;www.flaticon.com; is licensed by ;http://creativecommons.org/licenses/by/3.0/ ;Creative Commons BY 3.0
Water; made by ;https://www.freepik.com/ ;Freepik; from ;https://www.flaticon.com/ ;www.flaticon.com; is licensed by ;http://creativecommons.org/licenses/by/3.0/ ;Creative Commons BY 3.0
Salt; made by ;https://www.freepik.com/ ;Freepik; from ;https://www.flaticon.com/ ;www.flaticon.com; is licensed by ;http://creativecommons.org/licenses/by/3.0/ ;Creative Commons BY 3.0
Dish; made by ;https://www.flaticon.com/authors/photo3idea-studio ;photo3idea_studio; from ;https://www.flaticon.com/ ;www.flaticon.com; is licensed by ;http://creativecommons.org/licenses/by/3.0/ ;Creative Commons BY 3.0
TOMATO; made by ;https://www.flaticon.com/authors/smash ;Smash; from ;https://www.flaticon.com/ ;www.flaticon.com; is licensed by ;http://creativecommons.org/licenses/by/3.0/ ;Creative Commons BY 3.0
CHOCOLATE; made by ;https://www.flaticon.com/authors/smash ;Smash; from ;https://www.flaticon.com/ ;www.flaticon.com; is licensed by ;http://creativecommons.org/licenses/by/3.0/ ;Creative Commons BY 3.0
STRAWBERRY; made by ;https://www.flaticon.com/authors/smash ;Smash; from ;https://www.flaticon.com/ ;www.flaticon.com; is licensed by ;http://creativecommons.org/licenses/by/3.0/ ;Creative Commons BY 3.0
RASPBERRY; made by ;https://www.flaticon.com/authors/smash ;Smash; from ;https://www.flaticon.com/ ;www.flaticon.com; is licensed by ;http://creativecommons.org/licenses/by/3.0/ ;Creative Commons BY 3.0
APPLE; made by ;https://www.flaticon.com/authors/freepik ;Freepik; from ;https://www.flaticon.com/ ;www.flaticon.com; is licensed by ;http://creativecommons.org/licenses/by/3.0/ ;Creative Commons BY 3.0
HAZELNUT; made by ;https://www.flaticon.com/authors/smash ;Smash; from ;https://www.flaticon.com/ ;www.flaticon.com; is licensed by ;http://creativecommons.org/licenses/by/3.0/ ;Creative Commons BY 3.0
NUTS; made by ;https://www.flaticon.com/authors/dinosoftlabs ;DinosoftLabs; from ;https://www.flaticon.com/ ;www.flaticon.com; is licensed by ;http://creativecommons.org/licenses/by/3.0/ ;Creative Commons BY 3.0
PEANUT; made by ;https://www.flaticon.com/authors/twitter ;Twitter; from ;https://www.flaticon.com/ ;www.flaticon.com; is licensed by ;http://creativecommons.org/licenses/by/3.0/ ;Creative Commons BY 3.0
AVOCADO; made by ;https://www.flaticon.com/authors/vitaly-gorbachev ;Vitaly Gorbachev; from ;https://www.flaticon.com/ ;www.flaticon.com; is licensed by ;http://creativecommons.org/licenses/by/3.0/ ;Creative Commons BY 3.0
POTATO; made by ;https://www.flaticon.com/authors/smash ;Smash; from ;https://www.flaticon.com/ ;www.flaticon.com; is licensed by ;http://creativecommons.org/licenses/by/3.0/ ;Creative Commons BY 3.0
TOMATO SAUCE; made by ;https://www.flaticon.com/authors/freepik ;Freepik; from ;https://www.flaticon.com/ ;www.flaticon.com; is licensed by ;http://creativecommons.org/licenses/by/3.0/ ;Creative Commons BY 3.0
COFFEE; made by ;https://www.flaticon.com/authors/smash ;Smash; from ;https://www.flaticon.com/ ;www.flaticon.com; is licensed by ;http://creativecommons.org/licenses/by/3.0/ ;Creative Commons BY 3.0
PEAR; made by ;https://www.flaticon.com/authors/freepik ;Freepik; from ;https://www.flaticon.com/ ;www.flaticon.com; is licensed by ;http://creativecommons.org/licenses/by/3.0/ ;Creative Commons BY 3.0
COCONUT; made by ;https://www.flaticon.com/authors/vitaly-gorbachev ;Vitaly Gorbachev; from ;https://www.flaticon.com/ ;www.flaticon.com; is licensed by ;http://creativecommons.org/licenses/by/3.0/ ;Creative Commons BY 3.0
CHEESE; made by ;https://www.flaticon.com/authors/smash ;Smash; from ;https://www.flaticon.com/ ;www.flaticon.com; is licensed by ;http://creativecommons.org/licenses/by/3.0/ ;Creative Commons BY 3.0
HONEY; made by ;https://www.flaticon.com/authors/photo3idea-studio ;photo3idea_studio; from ;https://www.flaticon.com/ ;www.flaticon.com; is licensed by ;http://creativecommons.org/licenses/by/3.0/ ;Creative Commons BY 3.0
CARROT;;;; from ;https://fontawesome.com;fontawesome.com; is licensed by ;https://fontawesome.com/license ;Creative Commons Attribution 4.0 International
    """