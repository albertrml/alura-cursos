package br.com.alura.aluvery.utils

import br.com.alura.aluvery.model.Product
import kotlin.collections.filter

fun Map<String,List<Product>>.filter(query: String): Map<String, List<Product>> {
    return if(query.isEmpty()) this
    else{
        val filteredSections = mutableMapOf<String, List<Product>>()
        this.forEach { (sectionName, products) ->
            val filteredProducts = products.filter { product ->
                product.name.contains(query, ignoreCase = true)
            }
            if(filteredProducts.isNotEmpty()){
                filteredSections[sectionName] = filteredProducts
            }
        }
        filteredSections
    }
}