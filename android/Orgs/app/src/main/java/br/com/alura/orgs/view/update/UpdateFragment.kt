package br.com.alura.orgs.view.update

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import br.com.alura.orgs.R
import br.com.alura.orgs.databinding.FragmentUpdateBinding
import br.com.alura.orgs.model.entity.Item
import br.com.alura.orgs.model.entity.onCheck
import br.com.alura.orgs.utils.showResults
import br.com.alura.orgs.viemodel.OrgViewModel
import br.com.alura.orgs.view.udf.UiEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UpdateFragment : Fragment() {

    private val orgViewModel: OrgViewModel by viewModels()
    private val binding by lazy { FragmentUpdateBinding.inflate(layoutInflater) }
    private val args: UpdateFragmentArgs by navArgs()
    private val navigateToHome: (Unit) -> Unit = {
        this@UpdateFragment.findNavController().navigate(
            R.id.action_updateFragment_to_homeFragment
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

    private fun getItemToUpdate(): Item{
        return Item(
            id = args.itemId,
            itemName = binding.nameEdittext.text.toString(),
            itemDescription = binding.descriptionEdittext.text.toString(),
            itemValue = binding.priceEdittext.text.toString().toDouble(),
            quantityInStock = binding.quantityEdittext.text.toString().toInt()
        )
    }

    private fun setupListeners() {
        binding.updateButton.setOnClickListener {
                val itemToUpdate = getItemToUpdate()
                itemToUpdate.onCheck(
                    isValid = { update(it) },
                    isInvalid = { exception ->
                        Toast.makeText(
                            requireContext(),
                            exception.message, Toast.LENGTH_SHORT
                        ).show()
                    }
                )
        }

        binding.backButton.setOnClickListener { navigateToHome(Unit) }
        binding.failureReturnButton.setOnClickListener { navigateToHome(Unit) }
        binding.successReturnButton.setOnClickListener { navigateToHome(Unit) }
    }

    private fun setupScreen(){
        orgViewModel.onEvent(UiEvent.OnFetchById(args.itemId))
        orgViewModel.viewModelScope.launch {
            orgViewModel.uiState.collect{ state ->
                state.fetchItemByIdState.showResults(
                    successViewGroup = binding.updateLayout,
                    loadingViewGroup = binding.loadingLayout,
                    failureViewGroup = binding.failureLayout,
                    actionOnSuccess = { itemBeforeUpdate ->
                        screenItem(itemBeforeUpdate)
                    },
                    actionOnFailure = { exception ->
                        binding.failureTextview.text = exception.message
                    }
                )
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun screenItem(itemBeforeUpdate: Item){
        with(binding){
            nameEdittext.setText(itemBeforeUpdate.itemName)
            descriptionEdittext.setText(itemBeforeUpdate.itemDescription)
            priceEdittext.setText(itemBeforeUpdate.itemValue.toString())
            quantityEdittext.setText(itemBeforeUpdate.quantityInStock.toString())
        }
    }

    @SuppressLint("SetTextI18n")
    private fun update(itemToUpdate:Item){
        orgViewModel.onEvent(UiEvent.OnUpdate(itemToUpdate))
        orgViewModel.viewModelScope.launch {
            orgViewModel.uiState.collect{ state ->
                state.updateState.showResults(
                    successViewGroup = binding.successLayout,
                    loadingViewGroup = binding.loadingLayout,
                    failureViewGroup = binding.updateLayout,
                    actionOnSuccess = { data ->
                        binding.successTextview.text = getString(R.string.success_update)
                    },
                    actionOnFailure = { exception ->
                        Toast.makeText(
                            requireContext(),
                            exception.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                )
            }
        }
    }
}