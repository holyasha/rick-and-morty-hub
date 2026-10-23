package com.example.rick_and_morty_hub

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rick_and_morty_hub.ui.theme.RickAndMortyTheme

data class Character(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val gender: String,
    val origin: String,
    val imageRes: Int
)

class MainActivity : ComponentActivity() {

    private val characters = listOf(
        Character(1, "Rick Sanchez", "Alive", "Human", "Male", "Earth", R.drawable.character_rick),
        Character(2, "Morty Smith", "Alive", "Human", "Male", "Earth", R.drawable.character_morty),
        Character(3, "Summer Smith", "Alive", "Human", "Female", "Earth", R.drawable.character_summer),
        Character(4, "Beth Smith", "Alive", "Human", "Female", "Earth", R.drawable.character_beth),
        Character(5, "Jerry Smith", "Alive", "Human", "Male", "Earth", R.drawable.character_jerry),
        Character(6, "Birdperson", "Alive", "Birdperson", "Male", "Bird World", R.drawable.character_birdperson),
        Character(7, "Mr. Meeseeks", "Alive", "Meeseeks", "Male", "Earth", R.drawable.character_meeseeks),
        Character(8, "Squanchy", "Alive", "Cat", "Male", "Squanch Planet", R.drawable.character_squanchy),
        Character(9, "Summer's Friend", "Alive", "Human", "Female", "Earth", R.drawable.character_friend),
        Character(10, "Abradolf Lincler", "Dead", "Human", "Male", "Earth", R.drawable.character_abradolf),
        Character(11, "Pickle Rick", "Alive", "Animal", "Male", "Earth", R.drawable.character_pickle),
        Character(12, "Evil Morty", "Alive", "Human", "Male", "Earth", R.drawable.character_evil_morty)
    )

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(
            LocaleHelper.applyLanguage(newBase, AppSettings.getLanguage(newBase))
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Lifecycle", "MainActivity: onCreate")
        showContent()
    }

    override fun onStart() {
        super.onStart()
        Log.d("Lifecycle", "MainActivity: onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("Lifecycle", "MainActivity: onResume")

        val expectedLanguage = if (AppSettings.getLanguage(this) == "English") "en" else "ru"
        val currentLanguage = resources.configuration.locales[0].language

        if (currentLanguage != expectedLanguage) {
            recreate()
            return
        }
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

    private fun showContent() {
        setContent {
            RickAndMortyTheme(
                darkTheme = AppSettings.isDarkTheme(this),
                fontScale = AppSettings.getFontScale(this)
            ) {
                MainNavigation(characters = characters)
            }
        }
    }

    private fun showRestoreDialog(onRefresh: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(R.string.restore_backup)
            .setMessage(R.string.restore_backup_question)
            .setPositiveButton(R.string.restore) { _, _ ->
                BackupManager.restoreBackup(this)
                onRefresh()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showDeleteDialog(onRefresh: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_backup)
            .setMessage(R.string.delete_backup_question)
            .setPositiveButton(R.string.delete) { _, _ ->
                BackupManager.deleteBackup(this)
                onRefresh()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showBackupCreatedDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.backup_created)
            .setMessage(R.string.backup_created_message)
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    @Composable
    fun MainNavigation(characters: List<Character>) {
        val navController = rememberNavController()
        var refreshKey by remember { mutableStateOf(0) }

        NavHost(
            navController = navController,
            startDestination = "start",
            modifier = Modifier.fillMaxSize()
        ) {
            composable("start") {
                StartFragment(
                    onRegistrationClick = { navController.navigate("registration") },
                    onLoginClick = { navController.navigate("content") }
                )
            }

            composable("registration") {
                RegistrationFragment(
                    onRegisterSuccess = {
                        navController.popBackStack()
                    }
                )
            }

            composable("content") {
                ContentFragment(
                    characters = characters,
                    onSettingsClick = { navController.navigate("settings") },
                    onCharactersListClick = { },
                    onCreateCharacterClick = { }
                )
            }

            composable("settings") {
                SettingsFragment(
                    onSettingsChanged = {
                        refreshKey++
                        recreate()
                    },
                    onShowRestoreDialog = {
                        showRestoreDialog { recreate() }
                    },
                    onShowDeleteDialog = {
                        showDeleteDialog { recreate() }
                    },
                    onShowBackupCreatedDialog = {
                        showBackupCreatedDialog()
                    }
                )
            }
        }
    }
}
