package br.com.alura.orgs.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "item")
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name="name")
    val itemName: String,
    @ColumnInfo(name="description")
    val itemDescription: String,
    @ColumnInfo(name="value")
    val itemValue: Double,
    @ColumnInfo(name="quantity")
    val quantityInStock: Int
)

fun Item.onCheck(
    isValid: (Item) -> Unit,
    isInvalid: (Exception) -> Unit
) = when{
    itemName.isBlank() -> isInvalid(Exception("Invalid name"))
    itemDescription.isBlank() -> isInvalid(Exception("Invalid description"))
    itemValue < 0 -> isInvalid(Exception("Invalid value"))
    quantityInStock < 0 -> isInvalid(Exception("Invalid quantity"))
    else -> isValid(this)
}