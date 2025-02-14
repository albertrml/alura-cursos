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
import br.com.alura.orgs.databinding.FragmentLoginBinding
import br.com.alura.orgs.utils.data.showResults
import br.com.alura.orgs.viewmodel.account.AccountUiEvent
import br.com.alura.orgs.viewmodel.account.AccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private val accountViewModel: AccountViewModel by viewModels()
    private val binding by lazy { FragmentLoginBinding.inflate(layoutInflater) }

    private val navigateToHome: () -> Unit = {
        val action = LoginFragmentDirections
            .actionLoginFragmentToHomeFragment()
        this@LoginFragment.findNavController().navigate(action)
    }

    private val navigateToSignUp: () -> Unit = {
        val action = LoginFragmentDirections
            .actionLoginFragmentToSignUpFragment()
        this@LoginFragment.findNavController().navigate(action)
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
        binding.run{
            loginFormsLayout.visibility = View.VISIBLE
            loginLoadingLayout.visibility = View.GONE
            loginFailureLayout.visibility = View.GONE

            loginPasswordTextInputEditText.setText("")
            loginUsernameTextInputEditText.setText("")
        }
    }

    private fun setupListeners(){
        binding.loginSubmitButton.setOnClickListener {
            accountViewModel.onEvent(
                AccountUiEvent.OnAuthenticate(
                    username = binding.loginUsernameTextInputEditText.text.toString(),
                    password = binding.loginPasswordTextInputEditText.text.toString()
                )
            )
            setupScreenByViewModel()
        }

        binding.loginSignupTextView.setOnClickListener { navigateToSignUp() }

        binding.loginFailureButton.setOnClickListener {
            binding.loginFailureLayout.visibility = View.GONE
            binding.loginFormsLayout.visibility = View.VISIBLE
            binding.loginPasswordTextInputEditText.setText("")
        }
    }

    private fun setupScreenByViewModel(){
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    accountViewModel.uiState.collect { uiState ->
                        uiState.authenticateState.showResults(
                            binding.loginFormsLayout,
                            binding.loginLoadingLayout,
                            binding.loginFailureLayout,
                            actionOnSuccess = { _ -> navigateToHome() },
                            actionOnFailure = { error ->
                                binding.loginFailureTextview.text = error.message
                            }
                        )
                    }
                }
            }
        }
    }

}