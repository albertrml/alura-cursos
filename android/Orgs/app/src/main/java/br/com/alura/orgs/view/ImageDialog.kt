package br.com.alura.orgs.view

import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import br.com.alura.orgs.R
import br.com.alura.orgs.databinding.FragmentImageBinding
import coil.load

object ImageDialog {
    fun show(
        context: Context,
        onConfirm: (String) -> Unit,
        onCancel: () -> Unit
    ) {
        var urlImage = ""
        val binding = FragmentImageBinding.inflate(LayoutInflater.from(context))
        showImage(urlImage, binding.itemImageview)
        setPreview(binding)
        AlertDialog.Builder(context)
            .setView(binding.root)
            .setPositiveButton("Confirmar") { _, _ ->
                urlImage = binding.urlImageEdittext.text.toString()
                onConfirm(urlImage)
            }
            .setNegativeButton("Cancelar") { _, _ -> onCancel() }
            .show()
    }

    private fun setPreview(binding: FragmentImageBinding){
        binding.previewButton.setOnClickListener{
            showImage(
                binding.urlImageEdittext.text.toString(),
                binding.itemImageview
            )
        }
    }

    private fun showImage(url: String, imageView: ImageView){
        if(url.isNotBlank())
            imageView.load(url)
        else
            imageView.load(R.drawable.light_success_icon)
    }
}