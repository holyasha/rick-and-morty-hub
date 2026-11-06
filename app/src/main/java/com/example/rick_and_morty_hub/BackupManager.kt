package com.example.rick_and_morty_hub

import android.content.Context
import com.example.rick_and_morty_hub.data.CharacterRepository
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Резервная копия хранится отдельно от рабочего локального кэша.
 * Удаление резервной копии не удаляет кэш, а восстановление копирует
 * данные из файла обратно в кэш.
 */
object BackupManager {

    private const val BACKUP_FILE_NAME = "rick_and_morty_backup.json"
    private const val BACKUP_VERSION = 2

    data class OperationResult(
        val success: Boolean,
        val message: String,
        val characterCount: Int = 0
    )

    fun createBackup(context: Context): OperationResult {
        return try {
            val snapshot = CharacterRepository(context).cachedSnapshot()
            if (snapshot.characters.isEmpty()) {
                return OperationResult(
                    success = false,
                    message = "Невозможно создать резервную копию: локальный кэш пуст. Сначала загрузите персонажей."
                )
            }

            val root = JSONObject().apply {
                put("version", BACKUP_VERSION)
                put("created_at", System.currentTimeMillis())
                put("last_page", snapshot.lastPage)
                put("characters", JSONArray().apply {
                    snapshot.characters.forEach { character ->
                        put(JSONObject().apply {
                            put("id", character.id)
                            put("name", character.name)
                            put("status", character.status)
                            put("species", character.species)
                            put("gender", character.gender)
                            put("origin", character.origin)
                        })
                    }
                })
            }

            val target = backupFile(context)
            val temporary = File(context.filesDir, "$BACKUP_FILE_NAME.tmp")
            temporary.writeText(root.toString(2), Charsets.UTF_8)

            if (target.exists() && !target.delete()) {
                temporary.delete()
                return OperationResult(false, "Не удалось заменить старую резервную копию.")
            }
            if (!temporary.renameTo(target)) {
                temporary.delete()
                return OperationResult(false, "Не удалось сохранить файл резервной копии.")
            }

            OperationResult(
                success = true,
                message = "Резервная копия создана. Сохранено персонажей: ${snapshot.characters.size}.",
                characterCount = snapshot.characters.size
            )
        } catch (exception: Exception) {
            OperationResult(
                success = false,
                message = "Ошибка создания резервной копии: ${exception.localizedMessage ?: exception.javaClass.simpleName}"
            )
        }
    }

    /**
     * Проверяет файл и при успехе заменяет рабочий кэш содержимым копии.
     * При отсутствии/повреждении файла кэш не изменяется.
     */
    fun restoreBackup(context: Context): OperationResult {
        val file = backupFile(context)
        if (!file.exists()) {
            return OperationResult(
                success = false,
                message = "Резервная копия не найдена. Создайте её перед восстановлением."
            )
        }

        return try {
            val root = JSONObject(file.readText(Charsets.UTF_8))
            val version = root.optInt("version", -1)
            if (version != BACKUP_VERSION) {
                return OperationResult(
                    success = false,
                    message = "Не удалось восстановить данные: неподдерживаемая версия резервной копии."
                )
            }

            val array = root.optJSONArray("characters")
                ?: return OperationResult(false, "Файл резервной копии повреждён: список персонажей отсутствует.")

            if (array.length() == 0) {
                return OperationResult(false, "Резервная копия не содержит персонажей.")
            }

            val restored = ArrayList<Character>(array.length())
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index)
                    ?: return OperationResult(false, "Файл резервной копии повреждён: некорректная запись персонажа.")

                if (!item.has("id") || !item.has("name")) {
                    return OperationResult(false, "Файл резервной копии повреждён: у персонажа отсутствуют обязательные поля.")
                }

                restored += Character(
                    id = item.getInt("id"),
                    name = item.optString("name"),
                    status = item.optString("status"),
                    species = item.optString("species"),
                    gender = item.optString("gender"),
                    origin = item.optString("origin")
                )
            }

            val lastPage = root.optInt("last_page", 0)
            CharacterRepository(context).replaceCache(restored, lastPage)

            OperationResult(
                success = true,
                message = "Восстановление завершено. Загружено персонажей: ${restored.size}.",
                characterCount = restored.size
            )
        } catch (exception: Exception) {
            OperationResult(
                success = false,
                message = "Ошибка восстановления резервной копии: ${exception.localizedMessage ?: exception.javaClass.simpleName}"
            )
        }
    }

    fun deleteBackup(context: Context): OperationResult {
        val file = backupFile(context)
        if (!file.exists()) {
            return OperationResult(false, "Резервная копия уже отсутствует.")
        }

        return if (file.delete()) {
            OperationResult(
                success = true,
                message = "Резервная копия удалена. Локальный кэш не затронут."
            )
        } else {
            OperationResult(false, "Не удалось удалить файл резервной копии.")
        }
    }

    fun backupExists(context: Context): Boolean =
        backupFile(context).let { it.exists() && it.length() > 0L }

    private fun backupFile(context: Context) =
        File(context.filesDir, BACKUP_FILE_NAME)
}
