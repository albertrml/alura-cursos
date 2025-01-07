package br.com.alura.orgs.view.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import br.com.alura.orgs.R
import br.com.alura.orgs.application.ItemApplication
import br.com.alura.orgs.model.repository.ItemRepository

class HomeFragment : Fragment() {
    val itemViewModel: ItemViewModel by viewModels ()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }
}