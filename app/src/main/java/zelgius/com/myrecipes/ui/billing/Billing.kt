@file:OptIn(ExperimentalMaterial3Api::class)

package zelgius.com.myrecipes.ui.billing

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.CloudDownload
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zelgius.billing.model.Product
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.ui.AppTheme
import zelgius.com.myrecipes.ui.billing.viewModel.BillingViewModel
import zelgius.com.myrecipes.ui.common.pagerIndicator.PageIndicator
import zelgius.com.myrecipes.utils.conditional
import kotlin.math.absoluteValue

@Composable
fun PremiumFeature(
    modifier: Modifier = Modifier,
    viewModel: BillingViewModel = hiltViewModel(),
    clickableShape: RoundedCornerShape = CircleShape,
    content: @Composable BoxScope.(modifier: Modifier) -> Unit,
) {
    val isPremium by viewModel.isPremium.collectAsState(false)
    var showPremiumDialog by remember { mutableStateOf(false) }

    Box(
        modifier
            .width(IntrinsicSize.Min)
            .height(IntrinsicSize.Min)
    ) {
        content(
            Modifier
                .conditional(!isPremium) { alpha(0.6f) }
        )

        if (!isPremium)
            Box(
                Modifier
                    .clip(clickableShape)
                    .fillMaxSize()
                    .clickable { showPremiumDialog = true }
            )
    }

    if (showPremiumDialog) {
        BillingDialog(onClose = { showPremiumDialog = false }, viewModel)
    }
}

@Composable
fun BillingDialog(onClose: () -> Unit, viewModel: BillingViewModel) {
    val products by viewModel.products.collectAsStateWithLifecycle()

    val context = LocalActivity.current?: return

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .width(400.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column {
                Text(
                    stringResource(R.string.billing_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .padding(bottom = 8.dp, top = 16.dp)
                        .padding(horizontal = 16.dp)
                )

                BillingScreen(
                    products,
                    onProductClick = {
                        viewModel.startPurchase(
                            context,
                            it
                        )

                        onClose()
                    })
            }
        }
    }
}


@Composable
private fun BillingScreen(
    products: List<Product>,
    onProductClick: (Product) -> Unit = {}
) {
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        item {

            Text(
                stringResource(R.string.billing_description),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        item {
            PremiumDescription(modifier = Modifier.padding(top = 16.dp))
        }

        if (products.isEmpty())
            item {
                CircularProgressIndicator(modifier = Modifier.padding(vertical = 8.dp))
            }
        else
            items(products) { p ->
                ProductItem(
                    p,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                ) {
                    onProductClick(p)
                }
            }

    }
}

@Composable
private fun ProductItem(product: Product, modifier: Modifier = Modifier, onClick: () -> Unit) {
    FilledTonalButton(onClick = onClick, modifier = modifier) {
        Text(
            when (product.type) {
                Product.Type.Subscription -> stringResource(
                    id = R.string.billing_subscription,
                    product.formattedPrice
                )

                Product.Type.OneTime -> stringResource(
                    id = R.string.billing_one_time,
                    product.formattedPrice
                )
            }
        )
    }
}

@Preview
@Composable
private fun BillingScreenPreview() {
    AppTheme {
        BillingScreen(
            products = listOf(
                Product(
                    formattedPrice = "123 EUR",
                    type = Product.Type.OneTime
                ),
                Product(
                    formattedPrice = "56 EUR",
                    type = Product.Type.Subscription
                ),
            )
        )
    }
}

@Preview
@Composable
private fun BillingScreenPreviewEmpty() {
    AppTheme {
        BillingScreen(
            products = emptyList()
        )
    }
}

@Composable
private fun PremiumDescription(modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState(pageCount = {
        3
    })
    Column(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            pageSpacing = 8.dp,
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) { index ->
            val modifier = Modifier
                .height(300.dp)
                .clip(shape = MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
                .graphicsLayer {
                    // Calculate the absolute offset for the current page from the
                    // scroll position. We use the absolute value which allows us to mirror
                    // any effects for both directions
                    val pageOffset = (
                            (pagerState.currentPage - index) + pagerState
                                .currentPageOffsetFraction
                            ).absoluteValue

                    // We animate the alpha, between 50% and 100%
                    alpha = lerp(
                        start = 0.5f,
                        stop = 1f,
                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
                    )
                }

            when (index) {
                0 -> PremiumFeature1(modifier)
                1 -> PremiumFeature2(modifier)
                2 -> PremiumFeature3(modifier)
                else -> {}
            }
        }

        PageIndicator(
            numberOfPages = 3,
            selectedPage = pagerState.currentPage,
            selectedColor = MaterialTheme.colorScheme.tertiary,
            defaultColor = MaterialTheme.colorScheme.surfaceContainer,
            defaultRadius = 8.dp,
            space = 8.dp,
            selectedLength = 16.dp,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
@Preview
private fun PremiumFeature1(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(stringResource(R.string.billing_premium_feature_1))
    }
}

@Composable
@Preview
private fun PremiumFeature2(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.billing_premium_feature_2))
        Icon(
            Icons.TwoTone.CloudDownload,
            contentDescription = "",
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxSize()
        )
    }
}

@Composable
@Preview
private fun PremiumFeature3(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(stringResource(R.string.billing_premium_feature_3))
        Row(
            modifier = Modifier
                .padding(top = 8.dp)
                .height(IntrinsicSize.Min)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.r_1),
                contentDescription = "",
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
            )

            Column(
                Modifier
                    .fillMaxHeight()
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Image(
                    painter = painterResource(R.drawable.i_26),
                    contentDescription = "",
                    modifier = Modifier
                        .size(36.dp)
                )
                Image(
                    painter = painterResource(R.drawable.i_29),
                    contentDescription = "",
                    modifier = Modifier
                        .size(36.dp)

                )
                Image(
                    painter = painterResource(R.drawable.i_28),
                    contentDescription = "",
                    modifier = Modifier
                        .size(36.dp)

                )
            }
        }
    }
}

@Preview
@Composable
private fun PremiumDescriptionPreview() {
    AppTheme {
        PremiumDescription()
    }

}