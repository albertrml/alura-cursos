package br.com.alura.orgs.view.details

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import br.com.alura.orgs.R
import br.com.alura.orgs.databinding.FragmentDetailsBinding
import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.utils.showResults
import br.com.alura.orgs.view.image.ImageDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DetailsFragment : Fragment() {

    private val args: DetailsFragmentArgs by navArgs()
    private val binding: FragmentDetailsBinding by lazy {
        FragmentDetailsBinding.inflate(layoutInflater)
    }
    private val detailsViewModel: DetailsViewModel by viewModels()
    private val navigateToHome: () -> Unit = {
        this@DetailsFragment.findNavController().navigate(
            R.id.action_detailsFragment_to_homeFragment
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        setupScreen(args.itemId)
        setupListeners()

        return binding.root
    }

    private fun setupScreen(itemId: Int) {
        detailsViewModel.onEvent(DetailsUiEvent.OnFetchItemById(itemId))
        detailsViewModel.viewModelScope.launch {
            detailsViewModel.uiState.collect { state ->
                state.fetchItemByIdState.showResults(
                    successViewGroup = binding.detailsLayout,
                    loadingViewGroup = binding.detailsLoadingLayout,
                    failureViewGroup = binding.detailsFailureLayout,
                    actionOnSuccess = { itemUi -> setFields(itemUi) },
                    actionOnFailure = { exception ->
                        binding.detailsFailureTextview.text = exception.message
                    }
                )
            }
        }
    }

    private fun setFields(itemUi: ItemUi){
        with(binding){
            detailsNameTextview.text = itemUi.itemName.value
            detailsDescriptionTextview.text = itemUi.itemDescription.value
            detailsPriceTextview.text = itemUi.itemValue.value
            ImageDialog.loadImage(itemUi.itemUrl.value, detailsImageImageview)
        }
    }

    private fun setupListeners() {
        binding.detailsFailureReturnButton.setOnClickListener{ navigateToHome() }
    }

}