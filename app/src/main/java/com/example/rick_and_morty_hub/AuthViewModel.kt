package com.example.rick_and_morty_hub

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/**
 * Данные пользователя, созданные на экране регистрации.
 *
 * В текущем учебном приложении учётная запись хранится в памяти ViewModel.
 * Это демонстрация передачи данных между экранами, а не серверная авторизация.
 */
data class RegisteredUser(
    val firstName: String,
    val lastName: String,
    val login: String,
    val email: String,
    val password: String
)

enum class LoginError {
    ACCOUNT_NOT_REGISTERED,
    INVALID_CREDENTIALS
}

class AuthViewModel : ViewModel() {

    var registeredUser by mutableStateOf<RegisteredUser?>(null)
        private set

    var loginError by mutableStateOf<LoginError?>(null)
        private set

    val registeredLogin: String
        get() = registeredUser?.login.orEmpty()

    fun register(user: RegisteredUser) {
        registeredUser = user.copy(
            firstName = user.firstName.trim(),
            lastName = user.lastName.trim(),
            login = user.login.trim(),
            email = user.email.trim()
        )
        loginError = null
        Log.i(TAG, "Регистрация пользователя завершена")
    }

    fun authenticate(login: String, password: String): Boolean {
        val user = registeredUser

        loginError = when {
            user == null -> LoginError.ACCOUNT_NOT_REGISTERED
            !user.login.equals(login.trim(), ignoreCase = true) ||
                    user.password != password -> LoginError.INVALID_CREDENTIALS
            else -> null
        }

        if (loginError == null) {
            Log.i(TAG, "Пользователь успешно авторизован")
            return true
        }

        Log.w(TAG, "Неудачная попытка авторизации: $loginError")
        return false
    }

    fun clearLoginError() {
        loginError = null
    }

    companion object {
        private const val TAG = "Auth"
    }
}
