package br.com.alura.orgs.view

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
import br.com.alura.orgs.model.entity.ItemUi
import br.com.alura.orgs.model.entity.onCheck
import br.com.alura.orgs.utils.data.showResults
import br.com.alura.orgs.view.components.ImageDialog
import br.com.alura.orgs.viewmodel.update.UpdateUiEvent
import br.com.alura.orgs.viewmodel.update.UpdateViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UpdateFragment : Fragment() {

    private val args: UpdateFragmentArgs by navArgs()
    private val binding by lazy { FragmentUpdateBinding.inflate(layoutInflater) }
    private val navigateToHome: () -> Unit = {
        this@UpdateFragment.findNavController().navigate(
            R.id.action_updateFragment_to_homeFragment
        )
    }
    private val updateViewModel: UpdateViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        setupScreen(args.itemId)
        setupListeners()

        return binding.root
    }

    private fun setupScreen(itemId:Int){
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = updateViewModel

        updateViewModel.onEvent(UpdateUiEvent.OnFetchItemUiById(itemId))
        updateViewModel.viewModelScope.launch {
            updateViewModel.uiState.collect{ state ->
                state.fetchItemByIdState.showResults(
                    successViewGroup = binding.updateLayout,
                    loadingViewGroup = binding.updateLoadingLayout,
                    failureViewGroup = binding.updateFailureLayout,
                    actionOnSuccess = { itemBeforeUpdate ->
                        setUpdateScreen(itemBeforeUpdate)
                    },
                    actionOnFailure = { exception ->
                        binding.updateFailureTextview.text = exception.message
                    }
                )
            }
        }
    }

    private fun setUpdateScreen(itemBeforeUpdate: ItemUi){
        binding.updateNameEdittext.setText(itemBeforeUpdate.itemName)
        binding.updateDescriptionEdittext.setText(itemBeforeUpdate.itemDescription)
        binding.updatePriceEdittext.setText(itemBeforeUpdate.itemValue)
        binding.updateQuantityEdittext.setText(itemBeforeUpdate.quantityInStock)
        binding.updateUserOwnerEdittext.setText(itemBeforeUpdate.userOwner)
    }

    private fun setupListeners() {
        binding.updateButton.setOnClickListener {
            val itemToUpdate = getItemUiFromScreen()
            itemToUpdate.toItem().onCheck(
                isValid = { update(itemToUpdate) },
                isInvalid = { exception ->
                    Toast.makeText(
                        requireContext(),
                        exception.message, Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }

        binding.updateImageImageview.setOnClickListener {
            ImageDialog.show(
                context = requireContext(),
                onConfirm = { url ->
                    updateViewModel.onEvent(UpdateUiEvent.OnSaveUrlImage(url = url))
                },
                onCancel = { }
            )
        }
        binding.updateBackButton.setOnClickListener { navigateToHome() }
        binding.updateFailureReturnButton.setOnClickListener { navigateToHome() }
        binding.updateSuccessReturnButton.setOnClickListener { navigateToHome() }
    }

    private fun getItemUiFromScreen(): ItemUi {
        return ItemUi(
            id = args.itemId,
            itemName = binding.updateNameEdittext.text.toString(),
            itemDescription = binding.updateDescriptionEdittext.text.toString(),
            itemValue = binding.updatePriceEdittext.text.toString(),
            quantityInStock = binding.updateQuantityEdittext.text.toString(),
            itemUrl = updateViewModel.uiState.value.urlImage,
            userOwner = binding.updateUserOwnerEdittext.text.toString()
        )
    }

    @SuppressLint("SetTextI18n")
    private fun update(itemUiToUpdate:ItemUi){
        updateViewModel.onEvent(UpdateUiEvent.OnUpdate(itemUiToUpdate))
        updateViewModel.viewModelScope.launch {
            updateViewModel.uiState.collect{ state ->
                state.updateState.showResults(
                    successViewGroup = binding.updateSuccessLayout,
                    loadingViewGroup = binding.updateLoadingLayout,
                    failureViewGroup = binding.updateLayout,
                    actionOnSuccess = { _ ->
                        binding.updateSuccessTextview.text = getString(R.string.update_success_message)
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