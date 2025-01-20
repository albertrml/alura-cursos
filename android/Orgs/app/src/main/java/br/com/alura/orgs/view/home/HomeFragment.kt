package br.com.alura.orgs.view.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.alura.orgs.databinding.FragmentHomeBinding
import br.com.alura.orgs.model.entity.Item
import br.com.alura.orgs.utils.showResults
import br.com.alura.orgs.view.home.adapter.ItemAdapter
import br.com.alura.orgs.view.udf.UiEvent
import br.com.alura.orgs.viemodel.OrgViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class HomeFragment : Fragment() {
    private val orgViewModel: OrgViewModel by viewModels ()
    private val binding by lazy { FragmentHomeBinding.inflate(layoutInflater) }
    private lateinit var onEditListener: (Item) -> Unit
    private lateinit var onRemoveListener: (Item) -> Unit
    private lateinit var itemAdapter: ItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        setupListeners()
        setupScreen()

        return binding.root
    }

    private fun setupListeners() {
        onEditListener = { item ->
            val action = HomeFragmentDirections
                .actionHomeFragmentToUpdateFragment(item.id)
            this@HomeFragment.findNavController().navigate(action)
        }

        onRemoveListener = { item ->
            orgViewModel.onEvent(UiEvent.OnDelete(item))
        }

        binding.addItemButton.setOnClickListener{
            val action = HomeFragmentDirections
                .actionHomeFragmentToInsertFragment()
            this@HomeFragment.findNavController().navigate(action)
        }

        binding.tryAgainButton.setOnClickListener {
            orgViewModel.onEvent(UiEvent.OnFetchAllItems)
        }

    }

    private fun setupScreen(){
        orgViewModel.onEvent(UiEvent.OnFetchAllItems)
        orgViewModel.viewModelScope.launch {
            orgViewModel.uiState.collect { state ->
                state.fetchAllItemsState.showResults(
                    successViewGroup = binding.successLayout,
                    loadingViewGroup = binding.loadingLayout,
                    failureViewGroup = binding.failureLayout,
                    actionOnSuccess = { items ->
                        setRecyclerView(items)
                    },
                    actionOnFailure = { exception ->
                        binding.failLoadingText.text = exception.toString()
                    }
                )
            }
        }
    }

    private fun setRecyclerView(
        items: List<Item> = emptyList(),
        layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext())
    ) {
        itemAdapter = ItemAdapter(
            items = items,
            onEditClick = onEditListener,
            onRemoveClick = onRemoveListener
        )
        binding.recyclerView.adapter = itemAdapter
        binding.recyclerView.layoutManager = layoutManager
    }

}