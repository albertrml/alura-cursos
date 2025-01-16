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

fun Item.isValid(): Exception? = when{
    itemValue < 0 -> Exception("Invalid value")
    quantityInStock < 0 -> Exception("Invalid quantity")
    else -> null
}