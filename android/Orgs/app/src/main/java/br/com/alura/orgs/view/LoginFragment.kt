package br.com.alura.orgs.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import br.com.alura.orgs.databinding.FragmentLoginBinding
import br.com.alura.orgs.utils.data.Authenticate
import br.com.alura.orgs.viewmodel.account.AccountUiEvent
import br.com.alura.orgs.viewmodel.account.AccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private val accountViewModel: AccountViewModel by viewModels()
    private val binding by lazy { FragmentLoginBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setupListeners()
        return binding.root
    }

    private fun setupScreen(){}

    private fun setupListeners(){
        binding.loginSubmitButton.setOnClickListener {
            accountViewModel.onEvent(
                AccountUiEvent.OnAuthenticate(
                    username = binding.loginUsernameTextInputEditText.text.toString(),
                    password = binding.loginPasswordTextInputEditText.text.toString()
                )
            )
        }

        binding.loginSignupTextView.setOnClickListener {  }

        accountViewModel.viewModelScope.launch {
            accountViewModel.auth.collectLatest { auth ->
                if (auth is Authenticate.Login) {
                    val action = LoginFragmentDirections
                        .actionLoginFragmentToHomeFragment()
                    this@LoginFragment.findNavController().navigate(action)
                }
            }
        }
    }

}