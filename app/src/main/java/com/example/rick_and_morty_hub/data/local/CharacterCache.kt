package com.example.rick_and_morty_hub.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Формат записи, в котором персонаж хранится в локальном JSON-кэше.
 */
internal data class CharacterEntity(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val gender: String,
    val origin: String
)

/**
 * Низкоуровневый доступ к SharedPreferences.
 * Отвечает только за чтение и запись кэша.
 */
internal class CharacterCache(context: Context) {

    private val preferences = context.getSharedPreferences(
        PREFERENCES_NAME,
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

    /**
     * Добавляет страницу к существующим данным.
     * ID используется как ключ, поэтому персонажи не дублируются.
     */
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
            .putString(
                KEY_CHARACTERS,
                gson.toJson(characters.distinctBy { it.id }.sortedBy { it.id })
            )
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

    private companion object {
        const val PREFERENCES_NAME = "rick_and_morty_cache"
        const val KEY_CHARACTERS = "characters_json"
        const val KEY_LAST_PAGE = "last_loaded_page"
    }
}
