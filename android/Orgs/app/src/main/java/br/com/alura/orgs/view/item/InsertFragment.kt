package br.com.alura.orgs.view.item

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
import br.com.alura.orgs.model.entity.onCheck
import br.com.alura.orgs.utils.showResults
import br.com.alura.orgs.viemodel.OrgViewModel
import br.com.alura.orgs.view.udf.UiEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class InsertFragment : Fragment() {

    private val orgViewModel: OrgViewModel by viewModels()
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

    private fun getItemToInsert(): Item {
        return Item(
            itemName = binding.nameEdittext.text.toString(),
            itemDescription = binding.descriptionEdittext.text.toString(),
            itemValue = binding.priceEdittext.text.toString().toDouble(),
            quantityInStock = binding.quantityEdittext.text.toString().toInt()
        )
    }

    private fun insert(itemToInsert: Item){
        orgViewModel.onEvent(
            UiEvent.OnInsert(
                itemToInsert.itemName,
                itemToInsert.itemDescription,
                itemToInsert.itemValue.toString(),
                itemToInsert.quantityInStock.toString()
            )
        )

        orgViewModel.viewModelScope.launch {
            orgViewModel.uiState.collect{ state ->
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

    private fun setupScreen(){
        with(binding){
            nameEdittext.setText("")
            descriptionEdittext.setText("")
            priceEdittext.setText("0")
            quantityEdittext.setText("0")
        }
    }

    private fun setupListeners() {
        binding.insertButton.setOnClickListener {
            val itemToInsert: Item = getItemToInsert()
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

}