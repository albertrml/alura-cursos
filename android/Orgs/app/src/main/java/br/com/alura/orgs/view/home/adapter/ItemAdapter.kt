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
import java.text.NumberFormat
import java.util.Locale

class ItemAdapter(
    private val items: List<Item>,
    private val onEditClick: (Item) -> Unit,
    private val onRemoveClick: (Item) -> Unit
): RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val editButton: ImageView by lazy { view.findViewById(R.id.edit_button) }
        private val removeButton: ImageView by lazy { view.findViewById(R.id.remove_button) }
        private val nameTextView: TextView by lazy { view.findViewById(R.id.name_textview) }
        private val descriptionTextView: TextView by lazy { view.findViewById(R.id.description_textview) }
        private val priceTextView: TextView by lazy { view.findViewById(R.id.price_textview) }
        private val inStockTextView: TextView by lazy { view.findViewById(R.id.in_stock_textview) }
        private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)

        @SuppressLint("SetTextI18n")
        fun bind(item: Item){
            nameTextView.text = item.itemName
            descriptionTextView.text = item.itemDescription
            priceTextView.text = currencyFormatter.format(item.itemValue)
            inStockTextView.text = "${item.quantityInStock} pct"
        }

        fun bindListeners(
            item: Item,
            onEditClick: (Item) -> Unit,
            onRemoveClick: (Item) -> Unit
        ){
            editButton.setOnClickListener {
                onEditClick(item)
            }
            removeButton.setOnClickListener {
                onRemoveClick(item)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
        holder.bindListeners(
            items[position],
            onEditClick,
            onRemoveClick
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.org_item, parent, false)

        return ViewHolder(view)
    }

}