package com.example.rick_and_morty_hub.data.model

/**
 * Доменная модель персонажа, используемая интерфейсом и слоем данных.
 */
data class Character(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val gender: String,
    val origin: String
)

/**
 * Снимок локального кэша для резервного копирования и восстановления.
 */
data class CharacterCacheSnapshot(
    val characters: List<Character>,
    val lastPage: Int
)
