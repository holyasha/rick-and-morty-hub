package com.example.rick_and_morty_hub.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * DTO ответа Rick and Morty API.
 */
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

/**
 * Контракт сетевого API.
 */
interface RickAndMortyApi {
    @GET("api/character")
    suspend fun getCharacters(@Query("page") page: Int): CharacterPageDto
}

/**
 * Единственная точка создания Retrofit-клиента.
 */
object ApiClient {
    val api: RickAndMortyApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://rickandmortyapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RickAndMortyApi::class.java)
    }
}
