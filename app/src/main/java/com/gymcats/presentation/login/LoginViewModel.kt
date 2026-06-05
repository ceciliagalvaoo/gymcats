package com.gymcats.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymcats.data.repository.AccountRepository
import com.gymcats.data.repository.UserRepository
import com.gymcats.data.remote.auth.TokenManager
import com.gymcats.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isRegisterMode: Boolean = false,
    val email: String = "",
    val phone: String = "",
    val identifier: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val biometricLabel: String? = null,
    val isAuthenticated: Boolean = false,
    val nextRoute: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            accountRepository.biometricAccountFlow.collect { account ->
                _uiState.value = _uiState.value.copy(
                    biometricLabel = account?.email?.ifBlank { account.phone }
                )
            }
        }
    }

    fun setRegisterMode(value: Boolean) {
        _uiState.value = _uiState.value.copy(
            isRegisterMode = value,
            errorMessage = null
        )
    }

    fun setEmail(value: String) = update { copy(email = value) }
    fun setPhone(value: String) = update { copy(phone = value) }
    fun setIdentifier(value: String) = update { copy(identifier = value) }
    fun setPassword(value: String) = update { copy(password = value) }
    fun setConfirmPassword(value: String) = update { copy(confirmPassword = value) }

    fun submit() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val current = _uiState.value

            val result = if (current.isRegisterMode) {
                if (current.password != current.confirmPassword) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "As senhas não conferem."
                    )
                    return@launch
                }
                accountRepository.register(current.email, current.phone, current.password)
            } else {
                accountRepository.login(current.identifier, current.password)
            }

            result.onSuccess {
                tokenManager.getOrCreateToken()
                val next = if (userRepository.hasProfile()) Screen.Home.route else Screen.Onboarding.route
                _uiState.value = _uiState.value.copy(
                    isAuthenticated = true,
                    isLoading = false,
                    nextRoute = next
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = it.message ?: "Não foi possível entrar."
                )
            }
        }
    }

    fun onBiometricSuccess() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            accountRepository.loginWithBiometric()
                .onSuccess {
                    tokenManager.getOrCreateToken()
                    val next = if (userRepository.hasProfile()) Screen.Home.route else Screen.Onboarding.route
                    _uiState.value = _uiState.value.copy(
                        isAuthenticated = true,
                        isLoading = false,
                        nextRoute = next
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = it.message ?: "Não foi possível autenticar com biometria."
                    )
                }
        }
    }

    fun onBiometricError(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message)
    }

    fun forgotPassword() {
        _uiState.value = _uiState.value.copy(
            errorMessage = "Recuperação de senha por código será implementada no próximo passo. Por enquanto, entre com e-mail/número e senha."
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun update(block: LoginUiState.() -> LoginUiState) {
        _uiState.value = _uiState.value.block()
    }
}

