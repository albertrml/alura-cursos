package br.com.alura.orgs.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.alura.orgs.R
import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.utils.currencyFormat
import br.com.alura.orgs.view.components.ImageDialog

class ItemAdapter(
    private val items: List<ItemUi>,
    private val onEditClick: (ItemUi) -> Unit,
    private val onRemoveClick: (ItemUi) -> Unit,
    private val onDetailsClick: (ItemUi) -> Unit
): RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val editButton: ImageView by lazy { view.findViewById(R.id.edit_button) }
        private val removeButton: ImageView by lazy { view.findViewById(R.id.remove_button) }
        private val nameTextView: TextView by lazy { view.findViewById(R.id.item_name_textview) }
        private val descriptionTextView: TextView by lazy { view.findViewById(R.id.item_description_textview) }
        private val priceTextView: TextView by lazy { view.findViewById(R.id.item_price_textview) }
        private val inStockTextView: TextView by lazy { view.findViewById(R.id.item_quantity_textview) }
        private val imageView: ImageView by lazy { view.findViewById(R.id.item_imageview) }

        @SuppressLint("SetTextI18n")
        fun bind(itemUi: ItemUi){
            nameTextView.text = itemUi.itemName
            descriptionTextView.text = itemUi.itemDescription
            priceTextView.text = currencyFormat(value = itemUi.itemValue.toDouble())
            inStockTextView.text = "${itemUi.quantityInStock } pct"
            ImageDialog.loadImage(itemUi.itemUrl, imageView)
        }

        fun bindListeners(
            itemUi: ItemUi,
            onEditClick: (ItemUi) -> Unit = {},
            onRemoveClick: (ItemUi) -> Unit = {},
            onDetailsClick: (ItemUi) -> Unit = {}
        ){
            editButton.setOnClickListener {
                onEditClick(itemUi)
            }
            removeButton.setOnClickListener {
                onRemoveClick(itemUi)
            }

            imageView.setOnClickListener{
                onDetailsClick(itemUi)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
        holder.bindListeners(
            items[position],
            onEditClick,
            onRemoveClick,
            onDetailsClick
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list, parent, false)

        return ViewHolder(view)
    }

}