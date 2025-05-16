package br.com.alura.aluvery.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.alura.aluvery.R
import br.com.alura.aluvery.model.Product
import br.com.alura.aluvery.sampledata.sampleSections
import br.com.alura.aluvery.ui.components.CardProductItem
import br.com.alura.aluvery.ui.components.ProductsSection
import br.com.alura.aluvery.ui.theme.AluveryTheme
import br.com.alura.aluvery.utils.filter

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    sections: Map<String, List<Product>>
) {

    var query by rememberSaveable{ mutableStateOf("") }

    Column(
        modifier = modifier
    ){
        HomeScreenSearch(
            query = query,
            onQueryChange = { query = it },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        )

        if (query.isEmpty()){
            HomeScreenSections(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxSize(),
                sections = sections
            )
        } else{
            HomeScreenProducts(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxSize(),
                products = sections.filter(query).values.flatten()
            )
        }
    }
}

@Composable
fun HomeScreenSearch(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChange: (String) -> Unit
){
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        shape = RoundedCornerShape(100f),
        placeholder = {
            Text("Produto")
        },
        label = { Text("Search") },
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = "Ícone de busca"
            )
        },
    )
}

@Composable
fun HomeScreenSections(
    modifier: Modifier = Modifier,
    sections: Map<String, List<Product>>
){
    val sectionTitles = sections.keys.toList()
    LazyColumn(
        modifier = modifier,
        state = rememberLazyListState(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(sectionTitles) { title ->
            val products = sections[title] ?: emptyList()
            Spacer(Modifier)
            ProductsSection(
                title = title,
                products = products
            )
            Spacer(Modifier)
        }
    }
}

@Composable
fun HomeScreenProducts(
    modifier: Modifier = Modifier,
    products: List<Product>
){
    LazyColumn(
        modifier = modifier,
        state = rememberLazyListState(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(products) { product ->
            CardProductItem(product = product)
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun HomeScreenPreview() {
    AluveryTheme {
        Surface {
            HomeScreen(
                sections = sampleSections
            )
        }
    }
}