package com.example.rick_and_morty_hub

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rick_and_morty_hub.ui.theme.RickAndMortyTheme

class RegistrationActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val language = AppSettings.getLanguage(newBase)

        super.attachBaseContext(
            LocaleHelper.applyLanguage(newBase, language)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("Lifecycle", "RegistrationActivity: onCreate")

        setContent {
            RickAndMortyTheme(
                darkTheme = AppSettings.isDarkTheme(this),
                fontScale = AppSettings.getFontScale(this)
            ) {
                RegistrationScreen(
                    onRegister = { login ->
                        val resultIntent = Intent().apply {
                            putExtra("registered_login", login)
                        }

                        setResult(RESULT_OK, resultIntent)
                        finish()
                    }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("Lifecycle", "RegistrationActivity: onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("Lifecycle", "RegistrationActivity: onResume")
    }

    override fun onPause() {
        Log.d("Lifecycle", "RegistrationActivity: onPause")
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        Log.d("Lifecycle", "RegistrationActivity: onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Lifecycle", "RegistrationActivity: onDestroy")
    }
}

@Composable
fun RegistrationScreen(
    onRegister: (String) -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var firstNameError by remember { mutableStateOf(false) }
    var lastNameError by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var confirmPasswordError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = stringResource(R.string.registration_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = firstName,
            onValueChange = {
                firstName = it
                firstNameError = false
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(
                    stringResource(R.string.first_name)
                )
            },
            isError = firstNameError
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = lastName,
            onValueChange = {
                lastName = it
                lastNameError = false
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(
                    stringResource(R.string.last_name)
                )
            },
            isError = lastNameError
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = login,
            onValueChange = {
                login = it
                loginError = false
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(
                    stringResource(R.string.login)
                )
            },
            isError = loginError
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = false
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(
                    stringResource(R.string.email)
                )
            },
            isError = emailError
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = false
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(
                    stringResource(R.string.password)
                )
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            isError = passwordError
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                confirmPasswordError = false
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(
                    stringResource(R.string.confirm_password)
                )
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            isError = confirmPasswordError
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                firstNameError = firstName.isBlank()
                lastNameError = lastName.isBlank()
                loginError = login.isBlank()

                emailError =
                    email.isBlank() ||
                            !Patterns.EMAIL_ADDRESS
                                .matcher(email)
                                .matches()

                passwordError = password.isBlank()

                confirmPasswordError =
                    confirmPassword.isBlank() ||
                            password != confirmPassword

                if (
                    !firstNameError &&
                    !lastNameError &&
                    !loginError &&
                    !emailError &&
                    !passwordError &&
                    !confirmPasswordError
                ) {
                    onRegister(login)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                stringResource(R.string.register_button)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegistrationScreenPreview() {
    RickAndMortyTheme(
        darkTheme = true
    ) {
        RegistrationScreen(
            onRegister = {}
        )
    }
}