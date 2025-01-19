package br.com.alura.orgs.view.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import br.com.alura.orgs.utils.Response
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
    private lateinit var onAddItemListener: () -> Unit
    private lateinit var onTryAgainListener: () -> Unit
    private lateinit var itemAdapter: ItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setListeners()
        attachListeners()
        setRecyclerView()
        orgViewModel.onEvent(UiEvent.OnFetchAllItems)
        orgViewModel.viewModelScope.launch {
            orgViewModel.uiState.collect { state ->
                showResults(state.fetchAllItemsState)
            }
        }

        return binding.root
    }

    private fun attachListeners() {
        binding.tryAgainButton.setOnClickListener{ onTryAgainListener() }
        binding.addItemButton.setOnClickListener { onAddItemListener() }
    }

    private fun <T> isDataVisible(response: Response<T>) {
        with(binding) {
            when(response){
                is Response.Success -> {
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            listLayout.visibility = View.VISIBLE
                            loadingList.visibility = View.GONE
                            failLoadingList.visibility = View.GONE
                        },
                        1500
                    )
                }
                is Response.Loading -> {
                    listLayout.visibility = View.GONE
                    loadingList.visibility = View.VISIBLE
                    failLoadingList.visibility = View.GONE
                }
                is Response.Failure -> {
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            listLayout.visibility = View.GONE
                            loadingList.visibility = View.GONE
                            failLoadingList.visibility = View.VISIBLE
                        },
                        1500
                    )
                }
            }
        }
    }

    private fun setListeners() {
        onEditListener = { item ->
            val action = HomeFragmentDirections
                .actionHomeFragmentToUpdateFragment(item.id)
            this@HomeFragment.findNavController().navigate(action)
        }

        onRemoveListener = { item ->
            orgViewModel.onEvent(UiEvent.OnDelete(item))
        }

        onAddItemListener = {
            val action = HomeFragmentDirections
                .actionHomeFragmentToItemFragment()
            this@HomeFragment.findNavController().navigate(action)
        }

        onTryAgainListener = {
            orgViewModel.onEvent(UiEvent.OnFetchAllItems)
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

    private fun showResults(response: Response<List<Item>>) {
        when(response){
            is Response.Success -> {
                binding.recyclerView.apply {
                    setRecyclerView(response.result)
                }
                isDataVisible(response)
            }
            is Response.Loading -> {
                isDataVisible(response)
            }
            is Response.Failure -> {
                binding.failLoadingText.text = response.exception.toString()
                isDataVisible(response)
            }
        }
    }
}