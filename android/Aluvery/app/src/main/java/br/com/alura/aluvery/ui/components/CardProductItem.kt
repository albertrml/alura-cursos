package br.com.alura.aluvery.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.alura.aluvery.R
import br.com.alura.aluvery.extensions.toBrazilianCurrency
import br.com.alura.aluvery.model.Product
import br.com.alura.aluvery.sampledata.sampleProducts
import coil.compose.AsyncImage
import br.com.alura.aluvery.ui.theme.AluveryTheme

@Composable
fun CardProductItem(
    modifier: Modifier = Modifier,
    product: Product,
    elevation: Dp = 4.dp
) {
    Card(
        modifier
            .fillMaxWidth()
            .heightIn(150.dp),
        elevation = elevation
    ) {
        Column {
            AsyncImage(
                model = product.image,
                contentDescription = null,
                Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                placeholder = painterResource(id = R.drawable.placeholder),
                contentScale = ContentScale.Crop
            )
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colors.primaryVariant)
                    .padding(16.dp)
            ) {
                Text(
                    text = product.name
                )
                Text(
                    text = product.price.toBrazilianCurrency()
                )
            }
            Text(
                text = product.description?: "",
                Modifier
                    .padding(16.dp)
            )
        }
    }
}

@Preview
@Composable
private fun CardProductItemPreview() {
    val product = sampleProducts.random()
    AluveryTheme {
        Surface {
            CardProductItem(
                product = product
            )
        }
    }
}