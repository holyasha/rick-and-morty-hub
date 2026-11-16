package com.example.rick_and_morty_hub

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rick_and_morty_hub.ui.theme.RickAndMortyTheme

// Преобразовано в фрагмент для Лабораторной работы 2
// Навигация теперь управляется через MainActivity и Navigation Component
/*
class StartActivity : ComponentActivity() {

    private val registrationLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            if (result.resultCode == RESULT_OK) {
                val login = result.data?.getStringExtra("registered_login")

                if (!login.isNullOrBlank()) {
                    Log.d(
                        "StartActivity",
                        "Получен зарегистрированный логин: $login"
                    )
                }
            }
        }

    override fun attachBaseContext(newBase: Context) {
        val language = AppSettings.getLanguage(newBase)

        super.attachBaseContext(
            LocaleHelper.applyLanguage(newBase, language)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("Lifecycle", "StartActivity: onCreate")

        setContent {
            RickAndMortyTheme(
                darkTheme = AppSettings.isDarkTheme(this),
                fontScale = AppSettings.getFontScale(this)
            ) {
                StartScreen(
                    onRegistrationClick = {
                        openRegistration()
                    },
                    onContentClick = {
                        openContent()
                    },
                    onLoginClick = { _, _ ->
                        openContent()
                    }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("Lifecycle", "StartActivity: onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("Lifecycle", "StartActivity: onResume")
    }

    override fun onPause() {
        Log.d("Lifecycle", "StartActivity: onPause")
        super.onPause()
    }

    override fun onStop() {
        Log.d("Lifecycle", "StartActivity: onStop")
        super.onStop()
    }

    override fun onDestroy() {
        Log.d("Lifecycle", "StartActivity: onDestroy")
        super.onDestroy()
    }

    private fun openRegistration() {
        registrationLauncher.launch(
            android.content.Intent(
                this,
                RegistrationActivity::class.java
            )
        )
    }

    private fun openContent() {
        startActivity(
            android.content.Intent(
                this,
                ContentActivity::class.java
            )
        )

        finish()
    }
}
*/

@Composable
fun StartScreen(
    registeredLogin: String,
    loginError: LoginError?,
    onCredentialsChanged: () -> Unit,
    onRegistrationClick: () -> Unit,
    onLoginClick: (String, String) -> Unit
) {
    var login by remember(registeredLogin) {
        mutableStateOf(registeredLogin)
    }
    var password by remember { mutableStateOf("") }

    var loginFieldError by remember { mutableStateOf(false) }
    var passwordFieldError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Rick & Morty Hub",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.start_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = login,
            onValueChange = {
                login = it
                loginFieldError = false
                onCredentialsChanged()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.login)) },
            isError = loginFieldError,
            supportingText = if (loginFieldError) {
                { Text(stringResource(R.string.login_required)) }
            } else {
                null
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordFieldError = false
                onCredentialsChanged()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.password)) },
            visualTransformation = PasswordVisualTransformation(),
            isError = passwordFieldError,
            supportingText = if (passwordFieldError) {
                { Text(stringResource(R.string.password_required)) }
            } else {
                null
            }
        )

        val authErrorText = when (loginError) {
            LoginError.ACCOUNT_NOT_REGISTERED ->
                stringResource(R.string.account_not_registered)
            LoginError.INVALID_CREDENTIALS ->
                stringResource(R.string.invalid_credentials)
            null -> null
        }

        if (authErrorText != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = authErrorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                loginFieldError = login.isBlank()
                passwordFieldError = password.isBlank()

                if (!loginFieldError && !passwordFieldError) {
                    onLoginClick(login, password)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.login_button))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onRegistrationClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.registration))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StartScreenPreview() {
    RickAndMortyTheme(
        darkTheme = true
    ) {
        StartScreen(
            registeredLogin = "",
            loginError = null,
            onCredentialsChanged = {},
            onRegistrationClick = {},
            onLoginClick = { _, _ -> }
        )
    }
}