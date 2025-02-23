package br.com.alura.orgs.view

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
import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.model.entity.onCheck
import br.com.alura.orgs.utils.data.showResults
import br.com.alura.orgs.view.components.ImageDialog
import br.com.alura.orgs.viewmodel.insert.InsertUiEvent
import br.com.alura.orgs.viewmodel.insert.InsertViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class InsertFragment : Fragment() {

    private val insertViewModel: InsertViewModel by viewModels()
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
        binding.viewmodel = insertViewModel
        with(ItemUi()){
            binding.nameEdittext.setText(itemName)
            binding.descriptionEdittext.setText(itemDescription)
            binding.priceEdittext.setText(itemValue)
            binding.quantityEdittext.setText(quantityInStock)
        }
    }

    private fun setupListeners() {
        binding.insertButton.setOnClickListener {
            val itemToInsert: ItemUi = getItemUiFromScreen()
            itemToInsert.toItem().onCheck(
                isValid = { insert(itemToInsert) },
                isInvalid = { exception ->
                    Toast.makeText(
                        requireContext(),
                        exception.message, Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }

        binding.insertImageImageview.setOnClickListener {
            ImageDialog.show(
                context = requireContext(),
                onConfirm = { url ->
                    insertViewModel.onEvent(InsertUiEvent.OnSaveUrlImage(url = url))
                },
                onCancel = {}
            )
        }

        binding.insertBackButton.setOnClickListener { navigateToHome(Unit) }
        binding.insertSuccessReturnButton.setOnClickListener { navigateToHome(Unit) }
        binding.insertFailureReturnButton.setOnClickListener { navigateToHome(Unit) }
    }

    private fun getItemUiFromScreen(): ItemUi {
        return ItemUi(
            itemName = binding.nameEdittext.text.toString(),
            itemDescription = binding.descriptionEdittext.text.toString(),
            itemValue = binding.priceEdittext.text.toString(),
            quantityInStock = binding.quantityEdittext.text.toString(),
            itemUrl = insertViewModel.uiState.value.urlImage,
        )
    }

    private fun insert(itemUiToInsert: ItemUi){
        insertViewModel.onEvent(InsertUiEvent.OnInsert(itemUiToInsert))
        insertViewModel.viewModelScope.launch {
            insertViewModel.uiState.collect{ state ->
                state.insertState.showResults(
                    successViewGroup = binding.insertSuccessLayout,
                    loadingViewGroup = binding.insertLoadingLayout,
                    failureViewGroup = binding.insertFailureLayout,
                    actionOnSuccess = {
                        binding.insertLayout.visibility = View.GONE
                        binding.insertSuccessNameTextview.text = getString(R.string.insert_success_message)
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