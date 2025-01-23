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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UpdateFragment : Fragment() {

    private val updateViewModel: UpdateViewModel by viewModels()
    private val updateScreenViewModel: UpdateScreenViewModel by viewModels()
    private val binding by lazy { FragmentUpdateBinding.inflate(layoutInflater) }
    private val args: UpdateFragmentArgs by navArgs()
    private val navigateToHome: () -> Unit = {
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

    private fun setupScreen(){
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = updateScreenViewModel

        updateViewModel.onEvent(UpdateUiEvent.OnFetchItemById(args.itemId))
        updateViewModel.viewModelScope.launch {
            updateViewModel.uiState.collect{ state ->
                state.fetchItemByIdState.showResults(
                    successViewGroup = binding.updateLayout,
                    loadingViewGroup = binding.loadingLayout,
                    failureViewGroup = binding.failureLayout,
                    actionOnSuccess = { itemBeforeUpdate ->
                        updateScreenViewModel.fromItem(itemBeforeUpdate)
                    },
                    actionOnFailure = { exception ->
                        binding.failureTextview.text = exception.message
                    }
                )
            }
        }
    }

    private fun setupListeners() {
        binding.updateButton.setOnClickListener {
            val itemToUpdate = updateScreenViewModel.toItem()
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
        binding.backButton.setOnClickListener { navigateToHome() }
        binding.failureReturnButton.setOnClickListener { navigateToHome() }
        binding.successReturnButton.setOnClickListener { navigateToHome() }
    }

    @SuppressLint("SetTextI18n")
    private fun update(itemToUpdate:Item){
        updateViewModel.onEvent(UpdateUiEvent.OnUpdate(itemToUpdate))
        updateViewModel.viewModelScope.launch {
            updateViewModel.uiState.collect{ state ->
                state.updateState.showResults(
                    successViewGroup = binding.successLayout,
                    loadingViewGroup = binding.loadingLayout,
                    failureViewGroup = binding.updateLayout,
                    actionOnSuccess = { _ ->
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