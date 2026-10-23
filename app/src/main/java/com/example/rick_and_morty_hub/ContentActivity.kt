package com.example.rick_and_morty_hub

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rick_and_morty_hub.ui.theme.RickAndMortyTheme

data class Character(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val gender: String,
    val origin: String
)

// Преобразовано в фрагмент для Лабораторной работы 2
// Навигация теперь управляется через MainActivity и Navigation Component
/*
class ContentActivity : ComponentActivity() {

    private val characters = listOf(
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

    override fun attachBaseContext(newBase: Context) {
        val language = AppSettings.getLanguage(newBase)

        super.attachBaseContext(
            LocaleHelper.applyLanguage(newBase, language)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("Lifecycle", "ContentActivity: onCreate")

        showContent()
    }

    override fun onStart() {
        super.onStart()
        Log.d("Lifecycle", "ContentActivity: onStart")
    }

    override fun onResume() {
        super.onResume()

        Log.d("Lifecycle", "ContentActivity: onResume")

        val expectedLanguage =
            if (AppSettings.getLanguage(this) == "English") {
                "en"
            } else {
                "ru"
            }

        val currentLanguage =
            resources.configuration.locales[0].language

        if (currentLanguage != expectedLanguage) {
            recreate()
            return
        }

        showContent()
    }

    override fun onPause() {
        Log.d("Lifecycle", "ContentActivity: onPause")
        super.onPause()
    }

    override fun onStop() {
        Log.d("Lifecycle", "ContentActivity: onStop")
        super.onStop()
    }

    override fun onDestroy() {
        Log.d("Lifecycle", "ContentActivity: onDestroy")
        super.onDestroy()
    }

    private fun showContent() {
        setContent {
            RickAndMortyTheme(
                darkTheme = AppSettings.isDarkTheme(this),
                fontScale = AppSettings.getFontScale(this)
            ) {
                ContentScreen(
                    characters = characters,
                    onSettingsClick = {
                        startActivity(
                            android.content.Intent(
                                this,
                                SettingsActivity::class.java
                            )
                        )
                    }
                )
            }
        }
    }
}
*/

@Composable
fun ContentScreen(
    characters: List<Character>,
    onSettingsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 16.dp
            )
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = androidx.compose.ui.res.stringResource(
                    R.string.characters_title
                ),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Button(
                onClick = onSettingsClick
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(
                        R.string.settings
                    )
                )
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(characters) { character ->

                CharacterCard(character)

                Spacer(
                    modifier = Modifier.height(8.dp)
                )
            }
        }
    }
}

@Composable
fun CharacterCard(character: Character) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = character.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = androidx.compose.ui.res.stringResource(
                    R.string.character_status,
                    character.status
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = androidx.compose.ui.res.stringResource(
                    R.string.character_species,
                    character.species
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = androidx.compose.ui.res.stringResource(
                    R.string.character_gender,
                    character.gender
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = androidx.compose.ui.res.stringResource(
                    R.string.character_origin,
                    character.origin
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ContentScreenPreview() {

    RickAndMortyTheme(
        darkTheme = true
    ) {
        ContentScreen(
            characters = listOf(
                Character(
                    1,
                    "Rick Sanchez",
                    "Alive",
                    "Human",
                    "Male",
                    "Earth"
                ),
                Character(
                    2,
                    "Morty Smith",
                    "Alive",
                    "Human",
                    "Male",
                    "Earth"
                )
            ),
            onSettingsClick = {}
        )
    }
}