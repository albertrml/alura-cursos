package br.com.alura.orgs.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import br.com.alura.orgs.utils.exception.ItemException

@Entity(
    tableName = "item",
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["username"],
            childColumns = ["user_owner"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
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
    val quantityInStock: Int,
    @ColumnInfo(name="url_image")
    val itemUrl: String,
    @ColumnInfo(name="user_owner")
    val userOwner: String
)

fun Item.onCheck(
    isValid: (Item) -> Unit,
    isInvalid: (Exception) -> Unit
) = when{
    itemName.isBlank() -> isInvalid(ItemException.InvalidNameException())
    itemDescription.isBlank() -> isInvalid(ItemException.InvalidDescriptionException())
    itemValue < 0 -> isInvalid(ItemException.InvalidValueException())
    quantityInStock < 0 -> isInvalid(ItemException.InvalidQuantityException())
    else -> isValid(this)
}