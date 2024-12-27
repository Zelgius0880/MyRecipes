package zelgius.com.myrecipes.ui.common

import android.content.res.Resources
import android.graphics.Typeface
import android.text.Annotation
import android.text.Spanned
import android.text.SpannedString
import android.text.style.AbsoluteSizeSpan
import android.text.style.BulletSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.SubscriptSpan
import android.text.style.SuperscriptSpan
import android.text.style.TypefaceSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.core.text.HtmlCompat

const val URL_ANNOTATION_KEY = "url"

/**
 * Much of this class comes from
 * https://issuetracker.google.com/issues/139320238#comment11
 * which seeks to correct the gap in Jetpack Compose wherein HTML style tags in string resources
 * are not respected.
 */
@Composable
@ReadOnlyComposable
private fun resources(): Resources {
    return LocalContext.current.resources
}

fun Spanned.toHtmlWithoutParagraphs(): String {
    return HtmlCompat.toHtml(this, HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
        .substringAfter("<p dir=\"ltr\">").substringBeforeLast("</p>")
}

fun Resources.getText(@StringRes id: Int, vararg args: Any): CharSequence {
    val escapedArgs = args.map {
        if (it is Spanned) it.toHtmlWithoutParagraphs() else it
    }.toTypedArray()
    val resource = SpannedString(getText(id))
    val htmlResource = resource.toHtmlWithoutParagraphs()
    val formattedHtml = String.format(htmlResource, *escapedArgs)
    return HtmlCompat.fromHtml(formattedHtml, HtmlCompat.FROM_HTML_MODE_LEGACY)
}

@Composable
fun annotatedStringResource(
    @StringRes id: Int,
    vararg formatArgs: Any,
    onClick: (String) -> Unit = {}
): AnnotatedString {
    val resources = resources()
    val density = LocalDensity.current
    return remember(id, formatArgs) {
        val text = resources.getText(id, *formatArgs)
        spannableStringToAnnotatedString(text, density, onClick = onClick)
    }
}

@Composable
fun annotatedStringResource(
    @StringRes id: Int,
    style: SpanStyle? = null, onClick: (String) -> Unit = {}
): AnnotatedString {
    val resources = resources()
    val density = LocalDensity.current
    return remember(id) {
        val text = resources.getText(id)
        spannableStringToAnnotatedString(text, density, onClick = onClick, style = style)
    }
}

@OptIn(ExperimentalTextApi::class)
private fun spannableStringToAnnotatedString(
    text: CharSequence,
    density: Density,
    style: SpanStyle? = null,
    onClick: (String) -> Unit
): AnnotatedString {
    return if (text is Spanned) {
        with(density) {
            buildAnnotatedString {
                append((text.toString()))
                text.getSpans(0, text.length, Any::class.java).forEach {
                    val start = text.getSpanStart(it)
                    val end = text.getSpanEnd(it)
                    when (it) {
                        is StyleSpan -> when (it.style) {
                            Typeface.NORMAL -> addStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Normal,
                                    fontStyle = FontStyle.Normal
                                ),
                                start = start,
                                end = end
                            )

                            Typeface.BOLD -> addStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Normal
                                ),
                                start = start,
                                end = end
                            )

                            Typeface.ITALIC -> addStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Normal,
                                    fontStyle = FontStyle.Italic
                                ),
                                start = start,
                                end = end
                            )

                            Typeface.BOLD_ITALIC -> addStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic
                                ),
                                start = start,
                                end = end
                            )
                        }

                        is TypefaceSpan -> addStyle(
                            style = SpanStyle(
                                fontFamily = when (it.family) {
                                    FontFamily.SansSerif.name -> FontFamily.SansSerif
                                    FontFamily.Serif.name -> FontFamily.Serif
                                    FontFamily.Monospace.name -> FontFamily.Monospace
                                    FontFamily.Cursive.name -> FontFamily.Cursive
                                    else -> FontFamily.Default
                                }
                            ),
                            start = start,
                            end = end
                        )

                        is BulletSpan -> {
                            addStyle(style = SpanStyle(), start = start, end = end)
                        }

                        is AbsoluteSizeSpan -> addStyle(
                            style = SpanStyle(fontSize = if (it.dip) it.size.dp.toSp() else it.size.toSp()),
                            start = start,
                            end = end
                        )

                        is RelativeSizeSpan -> addStyle(
                            style = SpanStyle(fontSize = it.sizeChange.em),
                            start = start,
                            end = end
                        )

                        is StrikethroughSpan -> addStyle(
                            style = SpanStyle(textDecoration = TextDecoration.LineThrough),
                            start = start,
                            end = end
                        )

                        is UnderlineSpan -> addStyle(
                            style = SpanStyle(textDecoration = TextDecoration.Underline),
                            start = start,
                            end = end
                        )

                        is SuperscriptSpan -> addStyle(
                            style = SpanStyle(baselineShift = BaselineShift.Superscript),
                            start = start,
                            end = end
                        )

                        is SubscriptSpan -> addStyle(
                            style = SpanStyle(baselineShift = BaselineShift.Subscript),
                            start = start,
                            end = end
                        )

                        is ForegroundColorSpan -> addStyle(
                            style = SpanStyle(color = Color(it.foregroundColor)),
                            start = start,
                            end = end
                        )

                        is URLSpan -> {
                            addLink(
                                LinkAnnotation.Url(
                                    url = it.url,
                                    linkInteractionListener = { link ->
                                        onClick((link as LinkAnnotation.Url).url)
                                    },
                                ),
                                start = start,
                                end = end
                            )
                            addStyle(
                                style = style ?: SpanStyle(
                                    color = Color.Blue,
                                    textDecoration = TextDecoration.Underline
                                ),
                                start = start,
                                end = end
                            )

                        }

                        is Annotation -> {
                            if (it.key == URL_ANNOTATION_KEY) {
                                addStyle(
                                    style = SpanStyle(color = Color.Blue),
                                    start = start,
                                    end = end
                                )
                                LinkAnnotation.Clickable(
                                    it.value,
                                    linkInteractionListener = { link ->
                                        onClick(link.toString())
                                    })
                            }
                        }

                        else -> addStyle(style = SpanStyle(), start = start, end = end)
                    }
                }
            }
        }
    } else {
        AnnotatedString(text = text.toString())
    }
}

@Composable
fun LinkableText(
    @StringRes id: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    onClick: (String) -> Unit = {}
) {
    val annotatedString = annotatedStringResource(id, onClick)

    Text(
        text = annotatedString,
        style = style,
        /*onClick = { offset ->
            annotatedString.getStringAnnotations(
                tag = "URL",
                start = offset,
                end = offset
            ).firstOrNull()?.let {
                uriHandler.openUri(it.item)
            }
        },*/
        modifier = modifier,
    )
}
