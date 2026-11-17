package com.example.rick_and_morty_hub.data

import android.content.Context
import com.example.rick_and_morty_hub.data.local.CharacterCache
import com.example.rick_and_morty_hub.data.local.CharacterEntity
import com.example.rick_and_morty_hub.data.model.Character
import com.example.rick_and_morty_hub.data.model.CharacterCacheSnapshot
import com.example.rick_and_morty_hub.data.remote.ApiClient

/**
 * Координирует сетевой источник и локальный кэш.
 * UI и BackupManager работают с репозиторием, не обращаясь напрямую к Retrofit/SharedPreferences.
 */
class CharacterRepository(context: Context) {

    private val cache = CharacterCache(context.applicationContext)

    suspend fun cachedCharacters(): List<Character> =
        cache.getAll().map { it.toDomain() }

    fun cachedLastPage(): Int = cache.getLastPage()

    fun cachedSnapshot(): CharacterCacheSnapshot =
        CharacterCacheSnapshot(
            characters = cache.getAll().map { it.toDomain() },
            lastPage = cache.getLastPage()
        )

    fun replaceCache(characters: List<Character>, lastPage: Int) {
        cache.replaceAll(characters.map { it.toEntity() }, lastPage)
    }

    fun clearCache() = cache.clear()

    /** Загружает одну страницу API и добавляет её к локальным данным. */
    suspend fun loadPage(page: Int): PageResult {
        require(page > 0) { "Номер страницы должен быть положительным" }

        val response = ApiClient.api.getCharacters(page)
        val entities = response.results.orEmpty().map { dto ->
            CharacterEntity(
                id = dto.id,
                name = dto.name.orEmpty(),
                status = dto.status.orEmpty(),
                species = dto.species.orEmpty(),
                gender = dto.gender.orEmpty(),
                origin = dto.origin?.name.orEmpty()
            )
        }

        cache.append(entities, page)

        return PageResult(
            characters = entities.map { it.toDomain() },
            hasNext = !response.info?.next.isNullOrBlank()
        )
    }
}

data class PageResult(
    val characters: List<Character>,
    val hasNext: Boolean
)

private fun CharacterEntity.toDomain() = Character(
    id = id,
    name = name,
    status = status,
    species = species,
    gender = gender,
    origin = origin
)

private fun Character.toEntity() = CharacterEntity(
    id = id,
    name = name,
    status = status,
    species = species,
    gender = gender,
    origin = origin
)
