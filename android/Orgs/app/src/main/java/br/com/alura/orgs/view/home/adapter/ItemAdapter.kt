package br.com.alura.orgs.view.home.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.alura.orgs.R
import br.com.alura.orgs.model.entity.Item
import br.com.alura.orgs.view.image.ImageDialog
import currencyFormat
import java.text.NumberFormat
import java.util.Locale

class ItemAdapter(
    private val items: List<Item>,
    private val onEditClick: (Item) -> Unit,
    private val onRemoveClick: (Item) -> Unit,
    private val onDetailsClick: (Item) -> Unit
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
        fun bind(item: Item){
            nameTextView.text = item.itemName
            descriptionTextView.text = item.itemDescription
            priceTextView.text = currencyFormat(item.itemValue)
            inStockTextView.text = "${item.quantityInStock} pct"
            ImageDialog.loadImage(item.itemUrl, imageView)
        }

        fun bindListeners(
            item: Item,
            onEditClick: (Item) -> Unit = {},
            onRemoveClick: (Item) -> Unit = {},
            onDetailsClick: (Item) -> Unit = {}
        ){
            editButton.setOnClickListener {
                onEditClick(item)
            }
            removeButton.setOnClickListener {
                onRemoveClick(item)
            }

            imageView.setOnClickListener{
                onDetailsClick(item)
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
        /*val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.org_item, parent, false)*/
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list, parent, false)

        return ViewHolder(view)
    }

}