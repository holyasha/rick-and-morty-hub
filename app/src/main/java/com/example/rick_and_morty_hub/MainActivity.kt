package com.example.rick_and_morty_hub

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rick_and_morty_hub.ui.theme.RickAndMortyTheme

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val language = AppSettings.getLanguage(newBase)
        super.attachBaseContext(LocaleHelper.applyLanguage(newBase, language))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Lifecycle", "MainActivity: onCreate")

        setContent {
            RickAndMortyTheme(
                darkTheme = AppSettings.isDarkTheme(this),
                fontScale = AppSettings.getFontScale(this)
            ) {
                MainNavigation()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("Lifecycle", "MainActivity: onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("Lifecycle", "MainActivity: onResume")
    }

    override fun onPause() {
        Log.d("Lifecycle", "MainActivity: onPause")
        super.onPause()
    }

    override fun onStop() {
        Log.d("Lifecycle", "MainActivity: onStop")
        super.onStop()
    }

    override fun onDestroy() {
        Log.d("Lifecycle", "MainActivity: onDestroy")
        super.onDestroy()
    }
}

@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "start"
    ) {
        composable("start") {
            LifecycleLogger("StartScreen")
            StartScreenWrapper(navController)
        }

        composable("registration") {
            LifecycleLogger("RegistrationScreen")
            RegistrationScreenWrapper(navController)
        }

        composable("content") {
            LifecycleLogger("ContentScreen")
            ContentScreenWrapper(navController)
        }

        composable("settings") {
            LifecycleLogger("SettingsScreen")
            SettingsScreenWrapper(navController)
        }
    }
}

@Composable
fun LifecycleLogger(screenName: String) {
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> Log.d("Lifecycle", "$screenName: ON_CREATE")
                Lifecycle.Event.ON_START -> Log.d("Lifecycle", "$screenName: ON_START")
                Lifecycle.Event.ON_RESUME -> Log.d("Lifecycle", "$screenName: ON_RESUME")
                Lifecycle.Event.ON_PAUSE -> Log.d("Lifecycle", "$screenName: ON_PAUSE")
                Lifecycle.Event.ON_STOP -> Log.d("Lifecycle", "$screenName: ON_STOP")
                Lifecycle.Event.ON_DESTROY -> Log.d("Lifecycle", "$screenName: ON_DESTROY")
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

@Composable
fun StartScreenWrapper(navController: NavHostController) {
    StartScreen(
        onRegistrationClick = {
            navController.navigate("registration")
        },
        onContentClick = {
            navController.navigate("content") {
                popUpTo("start") { inclusive = true }
            }
        },
        onLoginClick = { _, _ ->
            navController.navigate("content") {
                popUpTo("start") { inclusive = true }
            }
        }
    )
}

@Composable
fun RegistrationScreenWrapper(navController: NavHostController) {
    RegistrationScreen(
        onRegister = { login ->
            Log.d("MainActivity", "Получен зарегистрированный логин: $login")
            navController.popBackStack()
        }
    )
}

@Composable
fun ContentScreenWrapper(navController: NavHostController) {
    val characters = listOf(
        Character(1, "Rick Sanchez", "Alive", "Human", "Male", "Earth"),
        Character(2, "Morty Smith", "Alive", "Human", "Male", "Earth"),
        Character(3, "Summer Smith", "Alive", "Human", "Female", "Earth"),
        Character(4, "Beth Smith", "Alive", "Human", "Female", "Earth"),
        Character(5, "Jerry Smith", "Alive", "Human", "Male", "Earth"),
        Character(6, "Birdperson", "Alive", "Birdperson", "Male", "Bird World"),
        Character(7, "Mr. Meeseeks", "Alive", "Meeseeks", "Male", "Earth"),
        Character(8, "Squanchy", "Alive", "Cat", "Male", "Squanch Planet"),
        Character(9, "Summer's Friend", "Alive", "Human", "Female", "Earth"),
        Character(10, "Abradolf Lincler", "Dead", "Human", "Male", "Earth"),
        Character(11, "Pickle Rick", "Alive", "Animal", "Male", "Earth"),
        Character(12, "Evil Morty", "Alive", "Human", "Male", "Earth")
    )

    ContentScreen(
        characters = characters,
        onSettingsClick = {
            navController.navigate("settings")
        }
    )
}

@Composable
fun SettingsScreenWrapper(navController: NavHostController) {
    val context = androidx.compose.ui.platform.LocalContext.current

    val darkTheme = AppSettings.isDarkTheme(context)
    val fontScale = AppSettings.getFontScale(context)
    val language = AppSettings.getLanguage(context)
    val notifications = AppSettings.isNotificationsEnabled(context)
    val backupExists = BackupManager.backupExists(context)

    SettingsScreen(
        initialDarkTheme = darkTheme,
        initialFontScale = fontScale,
        initialLanguage = language,
        initialNotifications = notifications,
        initialBackupExists = backupExists,
        onDarkThemeChanged = { enabled ->
            AppSettings.setDarkTheme(context, enabled)
            (context as? ComponentActivity)?.recreate()
        },
        onFontScaleChanged = { scale ->
            AppSettings.setFontScale(context, scale)
            (context as? ComponentActivity)?.recreate()
        },
        onLanguageChanged = { selectedLanguage ->
            if (selectedLanguage != AppSettings.getLanguage(context)) {
                AppSettings.setLanguage(context, selectedLanguage)
                (context as? ComponentActivity)?.recreate()
            }
        },
        onNotificationsChanged = { enabled ->
            AppSettings.setNotificationsEnabled(context, enabled)
        },
        onCreateBackup = {
            BackupManager.createBackup(context)
            android.app.AlertDialog.Builder(context)
                .setTitle(R.string.backup_created)
                .setMessage(R.string.backup_created_message)
                .setPositiveButton(R.string.ok, null)
                .show()
        },
        onRestoreBackup = {
            android.app.AlertDialog.Builder(context)
                .setTitle(R.string.restore_backup)
                .setMessage(R.string.restore_backup_question)
                .setPositiveButton(R.string.restore) { _, _ ->
                    BackupManager.restoreBackup(context)
                    (context as? ComponentActivity)?.recreate()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        },
        onDeleteBackup = {
            android.app.AlertDialog.Builder(context)
                .setTitle(R.string.delete_backup)
                .setMessage(R.string.delete_backup_question)
                .setPositiveButton(R.string.delete) { _, _ ->
                    BackupManager.deleteBackup(context)
                    (context as? ComponentActivity)?.recreate()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    )
}
