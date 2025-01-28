package br.com.alura.orgs.view.components

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
        loadImage(urlImage, binding.imageImageView)
        setPreview(binding)
        AlertDialog.Builder(context)
            .setView(binding.root)
            .setPositiveButton("Confirmar") { _, _ ->
                urlImage = binding.imageUrlEdittext.text.toString()
                onConfirm(urlImage)
            }
            .setNegativeButton("Cancelar") { _, _ -> onCancel() }
            .show()
    }

    private fun setPreview(binding: FragmentImageBinding){
        binding.imagePreviewButton.setOnClickListener{
            loadImage(
                binding.imageUrlEdittext.text.toString(),
                binding.imageImageView
            )
        }
    }

    fun loadImage(url: String, imageView: ImageView){
        if(url.isNotBlank())
            imageView.load(url)
        else
            imageView.load(R.drawable.ic_image_not_found)
    }
}