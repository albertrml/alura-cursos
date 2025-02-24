package br.com.alura.orgs.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.alura.orgs.R
import br.com.alura.orgs.databinding.FragmentHomeBinding
import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.utils.data.showResults
import br.com.alura.orgs.view.adapter.ItemAdapter
import br.com.alura.orgs.viewmodel.home.HomeUiEvent
import br.com.alura.orgs.viewmodel.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class HomeFragment : Fragment() {
    private val homeViewModel: HomeViewModel by viewModels ()
    private val binding by lazy { FragmentHomeBinding.inflate(layoutInflater) }
    private lateinit var onEditListener: (ItemUi) -> Unit
    private lateinit var onRemoveListener: (ItemUi) -> Unit
    private lateinit var onDetailsListener: (ItemUi) -> Unit
    private lateinit var onLogoutListener: () -> Unit
    private lateinit var onInsertListener: () -> Unit
    private lateinit var itemAdapter: ItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        setupListeners()
        setupButtons()
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

        onLogoutListener = {
            val action = HomeFragmentDirections.actionHomeFragmentToLoginFragment()
            this@HomeFragment.findNavController().navigate(action)
        }

        onInsertListener = {
            val action = HomeFragmentDirections
                .actionHomeFragmentToInsertFragment()
            this@HomeFragment.findNavController().navigate(action)
        }

    }

    private fun setupButtons(){
        binding.homeAddItemButton.setOnClickListener{ onInsertListener() }

        binding.homeTryAgainButton.setOnClickListener {
            selectFilter()
        }

        binding.homeLogoutButton.setOnClickListener{ onLogoutListener() }
    }

    private fun setupScreen(){
        selectFilter()
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
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
    }

    private fun selectFilter(){
        binding.homeFilterChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            when{
                checkedIds.contains(R.id.home_unsorted_filter_chip) -> {
                    homeViewModel.onEvent(HomeUiEvent.OnFetchAllItemsByIdAscending)
                }
                checkedIds.contains(R.id.home_name_filter_chip) -> {
                    homeViewModel.onEvent(HomeUiEvent.OnFetchAllItemsByNameAscending)
                }
                checkedIds.contains(R.id.home_price_filter_chip) -> {
                    homeViewModel.onEvent(HomeUiEvent.OnFetchAllItemsByPriceAscending)
                }
                checkedIds.contains(R.id.home_quantity_filter_chip) -> {
                    homeViewModel.onEvent(HomeUiEvent.OnFetchAllItemsByQuantityDescending)
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