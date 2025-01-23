package br.com.alura.orgs.view.components

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import br.com.alura.orgs.R
import coil.load

@BindingAdapter("imageUrl")
fun loadImage(view: ImageView, url: String) {
    if (url.isNotBlank())
        view.load(url)
    else
        view.load(R.drawable.light_success_icon)
}