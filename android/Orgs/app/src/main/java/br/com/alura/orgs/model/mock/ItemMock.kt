package br.com.alura.orgs.model.mock

import br.com.alura.orgs.model.entity.Item

val mockItems = listOf(
    Item(
        itemName = "Orange",
        itemDescription = "Fruit",
        itemValue = 0.35,
        quantityInStock = 10,
        itemUrl = ""
    ),
    Item(
        itemName = "Apple",
        itemDescription = "Fruit",
        itemValue = 0.50,
        quantityInStock = 15,
        itemUrl = ""
    ),
    Item(
        itemName = "Banana",
        itemDescription = "Fruit",
        itemValue = 0.25,
        quantityInStock = 20,
        itemUrl = ""
    ),
    Item(
        itemName = "Carrot",
        itemDescription = "Vegetable",
        itemValue = 0.75,
        quantityInStock = 8,
        itemUrl = ""
    ),
    Item(
        itemName = "Broccoli",
        itemDescription = "Vegetable",
        itemValue = 1.25,
        quantityInStock = 5,
        itemUrl = ""
    )
)