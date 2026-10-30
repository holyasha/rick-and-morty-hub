package com.example.rick_and_morty_hub

import android.content.Context
import com.example.rick_and_morty_hub.data.Character
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object BackupManager {

    private const val BACKUP_FILE_NAME = "rick_and_morty_backup.json"
    private val json = Json { prettyPrint = true }

    fun createBackup(context: Context, characters: List<Character>): Boolean {
        return try {
            val file = File(context.filesDir, BACKUP_FILE_NAME)
            file.writeText(json.encodeToString(characters))
            true
        } catch (exception: Exception) {
            exception.printStackTrace()
            false
        }
    }

    fun restoreBackup(context: Context): List<Character>? {
        return try {
            val file = File(context.filesDir, BACKUP_FILE_NAME)
            if (!file.exists()) return null
            json.decodeFromString<List<Character>>(file.readText())
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