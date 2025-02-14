package br.com.alura.orgs.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import br.com.alura.orgs.R
import br.com.alura.orgs.databinding.FragmentSignUpBinding
import br.com.alura.orgs.utils.data.showResults
import br.com.alura.orgs.viewmodel.account.AccountUiEvent
import br.com.alura.orgs.viewmodel.account.AccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private val accountViewModel: AccountViewModel by viewModels()
    private val binding by lazy { FragmentSignUpBinding.inflate(layoutInflater) }
    private val navigateToLogin: () -> Unit = {
        this@SignUpFragment.findNavController().navigate(
            R.id.action_signUpFragment_to_loginFragment
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setupListeners()
        return binding.root
    }

    override fun onResume() {
        setupScreen()
        super.onResume()
    }

    private fun setupScreen(){
        binding.run {
            signupFormsLayout.visibility = View.VISIBLE
            signupLoadingLayout.visibility = View.GONE
            signupFailureLayout.visibility = View.GONE

            signupUsernameTextInputEditText.setText("")
            signupPasswordTextInputEditText.setText("")
        }
    }

    private fun setupListeners(){
        binding.signupSubmitButton.setOnClickListener {
            accountViewModel.onEvent(
                AccountUiEvent.OnCreateAccount(
                    username = binding.signupUsernameTextInputEditText.text.toString(),
                    password = binding.signupPasswordTextInputEditText.text.toString()
                )
            )
            setupScreenByViewModel()
        }

        binding.signupBackButton.setOnClickListener{ navigateToLogin() }

        binding.signupFailureButton.setOnClickListener{
            binding.signupFailureLayout.visibility = View.GONE
            binding.signupFormsLayout.visibility = View.VISIBLE
            binding.signupPasswordTextInputEditText.setText("")
        }

    }

    private fun setupScreenByViewModel(){
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                launch {
                    accountViewModel.uiState.collect { uiState ->
                        uiState.createAccountState.showResults(
                            binding.signupFormsLayout,
                            binding.signupLoadingLayout,
                            binding.signupFailureLayout,
                            actionOnSuccess = { _ -> navigateToLogin() },
                            actionOnFailure = { error ->
                                binding.signupFailureTextview.text = error.message
                            }
                        )
                    }
                }
            }
        }
    }

}