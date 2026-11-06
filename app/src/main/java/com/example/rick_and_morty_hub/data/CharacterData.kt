package com.example.rick_and_morty_hub.data

import android.content.Context
import com.example.rick_and_morty_hub.Character
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/** DTO ответа Rick and Morty API. */
data class CharacterPageDto(
    val info: PageInfoDto?,
    val results: List<CharacterDto>?
)

data class PageInfoDto(
    val count: Int?,
    val pages: Int?,
    val next: String?,
    val prev: String?
)

data class CharacterDto(
    val id: Int,
    val name: String?,
    val status: String?,
    val species: String?,
    val gender: String?,
    val origin: OriginDto?
)

data class OriginDto(val name: String?)

interface RickAndMortyApi {
    @GET("api/character")
    suspend fun getCharacters(@Query("page") page: Int): CharacterPageDto
}

object ApiClient {
    val api: RickAndMortyApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://rickandmortyapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RickAndMortyApi::class.java)
    }
}

/** Локальная запись персонажа в SharedPreferences в формате JSON. */
data class CharacterEntity(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val gender: String,
    val origin: String
)

data class CharacterCacheSnapshot(
    val characters: List<Character>,
    val lastPage: Int
)

private class CharacterCache(context: Context) {
    private val preferences = context.getSharedPreferences(
        "rick_and_morty_cache",
        Context.MODE_PRIVATE
    )
    private val gson = Gson()
    private val listType = object : TypeToken<List<CharacterEntity>>() {}.type

    fun getAll(): List<CharacterEntity> {
        val json = preferences.getString(KEY_CHARACTERS, null) ?: return emptyList()
        return runCatching {
            gson.fromJson<List<CharacterEntity>>(json, listType) ?: emptyList()
        }.getOrDefault(emptyList())
    }

    /** Добавляет страницу к существующим данным, не удаляя предыдущие. */
    fun append(characters: List<CharacterEntity>, page: Int) {
        if (characters.isEmpty()) return

        val current = getAll().associateBy { it.id }.toMutableMap()
        characters.forEach { current[it.id] = it }

        preferences.edit()
            .putString(KEY_CHARACTERS, gson.toJson(current.values.sortedBy { it.id }))
            .putInt(KEY_LAST_PAGE, maxOf(page, getLastPage()))
            .apply()
    }

    /** Полностью заменяет кэш данными из резервной копии. */
    fun replaceAll(characters: List<CharacterEntity>, lastPage: Int) {
        preferences.edit()
            .putString(KEY_CHARACTERS, gson.toJson(characters.distinctBy { it.id }.sortedBy { it.id }))
            .putInt(KEY_LAST_PAGE, lastPage.coerceAtLeast(0))
            .apply()
    }

    fun clear() {
        preferences.edit()
            .remove(KEY_CHARACTERS)
            .remove(KEY_LAST_PAGE)
            .apply()
    }

    fun getLastPage(): Int = preferences.getInt(KEY_LAST_PAGE, 0)

    companion object {
        private const val KEY_CHARACTERS = "characters_json"
        private const val KEY_LAST_PAGE = "last_loaded_page"
    }
}

class CharacterRepository(context: Context) {
    private val cache = CharacterCache(context.applicationContext)

    suspend fun cachedCharacters(): List<Character> =
        cache.getAll().map { it.toCharacter() }

    fun cachedLastPage(): Int = cache.getLastPage()

    fun cachedSnapshot(): CharacterCacheSnapshot =
        CharacterCacheSnapshot(
            characters = cache.getAll().map { it.toCharacter() },
            lastPage = cache.getLastPage()
        )

    fun replaceCache(characters: List<Character>, lastPage: Int) {
        cache.replaceAll(
            characters.map { it.toEntity() },
            lastPage
        )
    }

    fun clearCache() = cache.clear()

    /** Загружает одну страницу и накапливает её в локальном хранилище. */
    suspend fun loadPage(page: Int): PageResult {
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
            characters = entities.map { it.toCharacter() },
            hasNext = !response.info?.next.isNullOrBlank()
        )
    }
}

data class PageResult(val characters: List<Character>, val hasNext: Boolean)

private fun CharacterEntity.toCharacter() =
    Character(id, name, status, species, gender, origin)

private fun Character.toEntity() =
    CharacterEntity(id, name, status, species, gender, origin)
