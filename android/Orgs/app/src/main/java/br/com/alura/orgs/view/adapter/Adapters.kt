package br.com.alura.orgs.view.adapter

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import br.com.alura.orgs.R
import coil.load

@BindingAdapter("imageUrl")
fun loadImage(view: ImageView, url: String) {
    if (url.isNotBlank())
        view.load(url)
    else
        view.load(R.drawable.ic_image_not_found)
}

