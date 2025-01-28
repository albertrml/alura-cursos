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
import br.com.alura.orgs.R
import br.com.alura.orgs.databinding.FragmentHomeBinding
import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.utils.SortType
import br.com.alura.orgs.utils.showResults
import br.com.alura.orgs.view.home.adapter.ItemAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class HomeFragment : Fragment() {
    private val homeViewModel: HomeViewModel by viewModels ()
    private val binding by lazy { FragmentHomeBinding.inflate(layoutInflater) }
    private lateinit var onEditListener: (ItemUi) -> Unit
    private lateinit var onRemoveListener: (ItemUi) -> Unit
    private lateinit var onDetailsListener: (ItemUi) -> Unit
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
            homeViewModel.onEvent(HomeUiEvent.OnDelete(item))
        }

        onDetailsListener = { item ->
            val action = HomeFragmentDirections
                .actionHomeFragmentToDetailsFragment(item.id)
            this@HomeFragment.findNavController().navigate(action)
        }

        binding.homeAddItemButton.setOnClickListener{
            val action = HomeFragmentDirections
                .actionHomeFragmentToInsertFragment()
            this@HomeFragment.findNavController().navigate(action)
        }

        binding.homeTryAgainButton.setOnClickListener {
            selectFilter()
        }

    }

    private fun setupScreen(){
        selectFilter()
        homeViewModel.viewModelScope.launch {
            homeViewModel.uiState.collect { state ->
                state.fetchAllItemsState.showResults(
                    successViewGroup = binding.homeSuccessLayout,
                    loadingViewGroup = binding.homeLoadingLayout,
                    failureViewGroup = binding.homeFailureLayout,
                    actionOnSuccess = { items ->
                        setRecyclerView(items)
                    },
                    actionOnFailure = { exception ->
                        binding.homeFailureTextview.text = exception.toString()
                    }
                )
            }
        }
    }

    private fun selectFilter(){
        binding.homeFilterChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            when{
                checkedIds.contains(R.id.home_unsorted_filter_chip) -> {
                    homeViewModel.onEvent(
                        HomeUiEvent.OnFetchAllItems(
                            sortBy = SortType.ByIdAscending
                        )
                    )
                }
                checkedIds.contains(R.id.home_name_filter_chip) -> {
                    homeViewModel.onEvent(
                        HomeUiEvent.OnFetchAllItems(
                            sortBy = SortType.ByNameAscending
                        )
                    )
                }
                checkedIds.contains(R.id.home_price_filter_chip) -> {
                    homeViewModel.onEvent(
                        HomeUiEvent.OnFetchAllItems(
                            sortBy = SortType.ByPriceAscending
                        )
                    )
                }
                checkedIds.contains(R.id.home_quantity_filter_chip) -> {
                    homeViewModel.onEvent(
                        HomeUiEvent.OnFetchAllItems(
                            sortBy = SortType.ByQuantityDescending
                        )
                    )
                }
            }
        }
    }

    private fun setRecyclerView(
        items: List<ItemUi> = emptyList(),
        layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext())
    ) {
        itemAdapter = ItemAdapter(
            items = items,
            onEditClick = onEditListener,
            onRemoveClick = onRemoveListener,
            onDetailsClick = onDetailsListener
        )
        binding.homeListRecyclerview.adapter = itemAdapter
        binding.homeListRecyclerview.layoutManager = layoutManager
    }

}