package com.example.rick_and_morty_hub

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object BackupManager {

    private const val BACKUP_FILE_NAME = "rick_and_morty_backup.json"

    fun createBackup(context: Context): Boolean {
        return try {
            val root = JSONObject()

            root.put("version", 1)
            root.put("created_at", System.currentTimeMillis())

            val characters = JSONArray()

            val sampleCharacters = listOf(
                Character(
                    id = 1,
                    name = "Rick Sanchez",
                    status = "Alive",
                    species = "Human",
                    gender = "Male",
                    origin = "Earth (C-137)"
                ),
                Character(
                    id = 2,
                    name = "Morty Smith",
                    status = "Alive",
                    species = "Human",
                    gender = "Male",
                    origin = "Earth (C-137)"
                ),
                Character(
                    id = 3,
                    name = "Summer Smith",
                    status = "Alive",
                    species = "Human",
                    gender = "Female",
                    origin = "Earth (Replacement Dimension)"
                )
            )

            sampleCharacters.forEach { character ->

                val jsonCharacter = JSONObject()

                jsonCharacter.put("id", character.id)
                jsonCharacter.put("name", character.name)
                jsonCharacter.put("status", character.status)
                jsonCharacter.put("species", character.species)
                jsonCharacter.put("gender", character.gender)
                jsonCharacter.put("origin", character.origin)

                characters.put(jsonCharacter)
            }

            root.put("characters", characters)

            val file = File(
                context.filesDir,
                BACKUP_FILE_NAME
            )

            file.writeText(
                root.toString(2)
            )

            true

        } catch (exception: Exception) {

            exception.printStackTrace()

            false
        }
    }

    fun restoreBackup(context: Context): List<Character>? {
        return try {

            val file = File(
                context.filesDir,
                BACKUP_FILE_NAME
            )

            if (!file.exists()) {
                return null
            }

            val json = JSONObject(
                file.readText()
            )

            val charactersJson =
                json.getJSONArray("characters")

            val characters = mutableListOf<Character>()

            for (index in 0 until charactersJson.length()) {

                val characterJson =
                    charactersJson.getJSONObject(index)

                characters.add(
                    Character(
                        id = characterJson.getInt("id"),
                        name = characterJson.getString("name"),
                        status = characterJson.getString("status"),
                        species = characterJson.getString("species"),
                        gender = characterJson.getString("gender"),
                        origin = characterJson.getString("origin")
                    )
                )
            }

            characters

        } catch (exception: Exception) {

            exception.printStackTrace()

            null
        }
    }

    fun deleteBackup(context: Context): Boolean {

        val file = File(
            context.filesDir,
            BACKUP_FILE_NAME
        )

        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }

    fun backupExists(context: Context): Boolean {

        val file = File(
            context.filesDir,
            BACKUP_FILE_NAME
        )

        return file.exists()
    }
}