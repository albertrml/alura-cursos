package br.com.alura.orgs.view.adapter

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import br.com.alura.orgs.R
import br.com.alura.orgs.utils.exception.AccountException
import br.com.alura.orgs.utils.tools.isPasswordValid
import coil.load
import com.google.android.material.textfield.TextInputLayout

@BindingAdapter("imageUrl")
fun loadImage(view: ImageView, url: String) {
    if (url.isNotBlank())
        view.load(url)
    else
        view.load(R.drawable.ic_image_not_found)
}

@BindingAdapter("android:password")
fun loadPassword(view: TextInputLayout, password: String) {
    if (!password.isPasswordValid()) {
        view.isErrorEnabled
        view.error = AccountException.InvalidPassword().message
    }
    else {
        view.isErrorEnabled = false
    }
}