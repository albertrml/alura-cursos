package br.com.alura.orgs.model.mock

import br.com.alura.orgs.model.entity.Item

private val organics = mapOf(
    "Fruit" to listOf("Orange", "Apple", "Banana", "Grape", "Strawberry", "Pineapple"),
    "Vegetable" to listOf("Carrot", "Broccoli", "Potato", "Tomato", "Onion",
                          "Lettuce", "Cucumber", "Spinach", "Cabbage", "Pepper",
                          "Eggplant", "Zucchini", "Garlic", "Ginger")
)

private val usernames = mockAccounts.map { it.username }

private fun randomUsername() = usernames.random()

private fun randomOrganics(): Map<String,String> {
    val description = organics.keys.random()
    val name = organics[description]!!.random()
    return mapOf(name to description)
}

private fun randomValue() = (10..100).random().toDouble() / 10

private fun randomQuantity() = (10..100).random()

val mockItems: List<Item> = List(20) { _ ->
    val organic = randomOrganics()
    val userOwner = randomUsername()
    Item(
        itemName = organic.keys.first(),
        itemDescription = organic.values.first(),
        itemValue = randomValue(),
        quantityInStock = randomQuantity(),
        itemUrl = "",
        userOwner = userOwner
    )
}

/*
val mockItems = listOf(
    Item(
        itemName = "Orange",
        itemDescription = "Fruit",
        itemValue = 0.35,
        quantityInStock = 10,
        itemUrl = "",
        userOwner = "richard"
    ),
    Item(
        itemName = "Apple",
        itemDescription = "Fruit",
        itemValue = 0.50,
        quantityInStock = 15,
        itemUrl = "",
        userOwner = "joao"
    ),
    Item(
        itemName = "Banana",
        itemDescription = "Fruit",
        itemValue = 0.25,
        quantityInStock = 20,
        itemUrl = "",
        userOwner = "maria"
    ),
    Item(
        itemName = "Carrot",
        itemDescription = "Vegetable",
        itemValue = 0.75,
        quantityInStock = 8,
        itemUrl = "",
        userOwner = "jose"
    ),
    Item(
        itemName = "Broccoli",
        itemDescription = "Vegetable",
        itemValue = 1.25,
        quantityInStock = 5,
        itemUrl = "",
        userOwner = "pedro"
    )
)*/
