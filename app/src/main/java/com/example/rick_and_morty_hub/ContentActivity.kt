package com.example.rick_and_morty_hub

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.rick_and_morty_hub.data.CharacterRepository
import kotlinx.coroutines.launch

 data class Character(
    val id: Int, val name: String, val status: String,
    val species: String, val gender: String, val origin: String
)

@Composable
fun ContentScreen(onSettingsClick: () -> Unit) {
    val context = LocalContext.current
    val repository = remember(context) { CharacterRepository(context) }
    val userPreferences by UserPreferencesStore.preferences(context).collectAsState(initial = UserPreferences())
    val scope = rememberCoroutineScope()
    var characters by remember { mutableStateOf<List<Character>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var hasNext by remember { mutableStateOf(true) }
    var nextPage by remember { mutableStateOf(1) }
    var error by remember { mutableStateOf<String?>(null) }
    var isOfflineCache by remember { mutableStateOf(false) }

    suspend fun loadPage(page: Int) {
        if (isLoading) return
        isLoading = true
        error = null
        try {
            val result = repository.loadPage(page)
            if (page == 1) {
                // После обновления первой страницы показываем весь накопленный кэш.
                characters = repository.cachedCharacters()
            } else {
                val merged = (characters + result.characters).associateBy { it.id }
                characters = merged.values.sortedBy { it.id }
            }
            hasNext = result.hasNext
            nextPage = maxOf(page + 1, repository.cachedLastPage() + 1)
            isOfflineCache = false
            if (result.characters.isEmpty() && page == 1) {
                error = "Сервер не вернул персонажей"
            }
        } catch (exception: Exception) {
            if (page == 1) {
                val cached = runCatching { repository.cachedCharacters() }.getOrDefault(emptyList())
                characters = cached
                isOfflineCache = cached.isNotEmpty()
                hasNext = false
                nextPage = repository.cachedLastPage() + 1
            }
            error = if (exception.message.isNullOrBlank()) {
                "Не удалось получить данные. Проверьте подключение к интернету."
            } else {
                "Ошибка загрузки: ${exception.localizedMessage ?: exception.javaClass.simpleName}"
            }
        } finally {
            isLoading = false
        }
    }

    // Сначала показываем локальные данные, затем пробуем обновить первую страницу.
    LaunchedEffect(Unit) {
        val cached = runCatching { repository.cachedCharacters() }.getOrDefault(emptyList())
        if (cached.isNotEmpty()) {
            characters = cached
            isOfflineCache = true
            nextPage = repository.cachedLastPage() + 1
        }
        loadPage(1)
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(R.string.characters_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Button(onClick = onSettingsClick) {
                Text(androidx.compose.ui.res.stringResource(R.string.settings))
            }
        }
        Spacer(Modifier.height(12.dp))
        if (isOfflineCache) {
            Text(
                "Нет подключения. Показаны сохранённые данные.",
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.height(8.dp))
        }
        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
            if (characters.isEmpty()) {
                Button(onClick = { scope.launch { loadPage(1) } }, enabled = !isLoading) {
                    Text("Повторить")
                }
            }
        }
        if (isLoading && characters.isEmpty()) {
            CircularProgressIndicator()
        }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(characters, key = { it.id }) { character ->
                CharacterCard(character, showOrigin = userPreferences.showCharacterOrigin)
                Spacer(Modifier.height(8.dp))
            }
            if (hasNext && characters.isNotEmpty()) {
                item {
                    Button(
                        onClick = { scope.launch { loadPage(nextPage) } },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isLoading) CircularProgressIndicator() else Text("Загрузить ещё")
                    }
                }
            }
        }
    }
}

@Composable
fun CharacterCard(character: Character, showOrigin: Boolean = true) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                character.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(6.dp))
            Text(androidx.compose.ui.res.stringResource(R.string.character_status, character.status))
            Text(androidx.compose.ui.res.stringResource(R.string.character_species, character.species))
            Text(androidx.compose.ui.res.stringResource(R.string.character_gender, character.gender))
            if (showOrigin) {
                Text(androidx.compose.ui.res.stringResource(R.string.character_origin, character.origin))
            }
        }
    }
}
