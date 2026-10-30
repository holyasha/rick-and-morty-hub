package com.example.rick_and_morty_hub

import android.content.Context

object AuthManager {
    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_LOGIN = "login"
    private const val KEY_PASSWORD = "password"
    private const val KEY_FIRST_NAME = "first_name"
    private const val KEY_LAST_NAME = "last_name"
    private const val KEY_EMAIL = "email"

    fun saveUser(
        context: Context,
        login: String,
        password: String,
        firstName: String,
        lastName: String,
        email: String
    ) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
            putString(KEY_LOGIN, login)
            putString(KEY_PASSWORD, password)
            putString(KEY_FIRST_NAME, firstName)
            putString(KEY_LAST_NAME, lastName)
            putString(KEY_EMAIL, email)
            apply()
        }
    }

    fun isRegistered(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.contains(KEY_LOGIN) && prefs.contains(KEY_PASSWORD)
    }

    fun validateLogin(context: Context, login: String, password: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedLogin = prefs.getString(KEY_LOGIN, null)
        val savedPassword = prefs.getString(KEY_PASSWORD, null)
        return login == savedLogin && password == savedPassword
    }
}
