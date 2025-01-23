package br.com.alura.orgs.view.insert

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import br.com.alura.orgs.R
import br.com.alura.orgs.databinding.FragmentInsertBinding
import br.com.alura.orgs.model.entity.Item
import br.com.alura.orgs.model.entity.emptyItem
import br.com.alura.orgs.model.entity.onCheck
import br.com.alura.orgs.utils.showResults
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class InsertFragment : Fragment() {

    private val insertViewModel: InsertViewModel by viewModels()
    private val insertScreenViewModel: InsertScreenViewModel by viewModels()
    private val binding by lazy { FragmentInsertBinding.inflate(layoutInflater) }
    private val navigateToHome: (Unit) -> Unit = {
        this@InsertFragment.findNavController().navigate(
            R.id.action_insertFragment_to_homeFragment
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        setupScreen()
        setupListeners()

        return binding.root
    }

    private fun setupScreen(){
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = insertScreenViewModel
        insertScreenViewModel.fromItem(emptyItem())
    }

    private fun setupListeners() {
        binding.insertButton.setOnClickListener {
            val itemToInsert: Item = insertScreenViewModel.toItem()
            itemToInsert.onCheck(
                isValid = { insert(it) },
                isInvalid = { exception ->
                    Toast.makeText(
                        requireContext(),
                        exception.message, Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }

        binding.backButton.setOnClickListener { navigateToHome(Unit) }
        binding.successReturnButton.setOnClickListener { navigateToHome(Unit) }
        binding.failureReturnButton.setOnClickListener { navigateToHome(Unit) }
    }

    private fun insert(itemToInsert: Item){
        insertViewModel.onEvent(InsertUiEvent.OnInsert(itemToInsert))
        insertViewModel.viewModelScope.launch {
            insertViewModel.uiState.collect{ state ->
                state.insertState.showResults(
                    successViewGroup = binding.successLayout,
                    loadingViewGroup = binding.loadingLayout,
                    failureViewGroup = binding.failureLayout,
                    actionOnSuccess = {
                        binding.insertLayout.visibility = View.GONE
                        binding.successTextview.text = getString(R.string.success_insert)
                    },
                    actionOnFailure = { exception ->
                        Toast.makeText(
                            requireContext(),
                            exception.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        }
    }

}