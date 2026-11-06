package com.example.rick_and_morty_hub

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.rick_and_morty_hub.data.CharacterRepository
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.DateFormat
import java.util.Date

/** Экспорт персонажей в общедоступный файл Downloads и отдельная внутренняя копия. */
object BackupManager {
    private const val FILE_NAME = "RickAndMortyHub_characters.txt"
    private const val RELATIVE_DIRECTORY = "${Environment.DIRECTORY_DOWNLOADS}/RickAndMortyHub/"
    private const val BACKUP_VERSION = 1

    data class OperationResult(
        val success: Boolean,
        val message: String,
        val characterCount: Int = 0
    )

    data class BackupInfo(
        val externalExists: Boolean,
        val internalExists: Boolean,
        val description: String
    )

    fun createBackup(context: Context): OperationResult {
        return try {
            val snapshot = CharacterRepository(context).cachedSnapshot()
            if (snapshot.characters.isEmpty()) {
                return OperationResult(false, "Невозможно создать файл: локальный список персонажей пуст. Сначала загрузите персонажей.")
            }
            val content = serialize(snapshot.characters, snapshot.lastPage)
            writeExternalFile(context, content)
            OperationResult(true, "Файл $FILE_NAME сохранён в Downloads/RickAndMortyHub. Персонажей: ${snapshot.characters.size}.", snapshot.characters.size)
        } catch (error: Exception) {
            OperationResult(false, "Ошибка создания файла: ${error.localizedMessage ?: error.javaClass.simpleName}")
        }
    }

    /** Сначала копирует внешний файл во внутреннее хранилище и только затем удаляет внешний. */
    fun deleteBackup(context: Context): OperationResult {
        return try {
            val content = readExternalFile(context)
                ?: return OperationResult(false, "Файл резервной копии в Downloads не найден.")
            val internal = internalBackupFile(context)
            val temporary = File(internal.parentFile, "${internal.name}.tmp")
            temporary.writeText(content, Charsets.UTF_8)
            if (!temporary.renameTo(internal)) {
                temporary.copyTo(internal, overwrite = true)
                temporary.delete()
            }
            if (!deleteExternalFile(context)) {
                return OperationResult(false, "Внутренняя резервная копия сохранена, но удалить внешний файл не удалось.")
            }
            OperationResult(true, "Внешний файл удалён. Его резервная копия сохранена во внутреннем хранилище приложения.")
        } catch (error: Exception) {
            OperationResult(false, "Ошибка удаления файла: ${error.localizedMessage ?: error.javaClass.simpleName}")
        }
    }

    /** Восстанавливает файл из internal storage обратно в Downloads и обновляет локальный кэш. */
    fun restoreBackup(context: Context): OperationResult {
        val internal = internalBackupFile(context)
        if (!internal.exists() || internal.length() == 0L) {
            return OperationResult(false, "Внутренняя резервная копия не найдена. Сначала создайте файл и удалите его с сохранением копии.")
        }
        return try {
            val content = internal.readText(Charsets.UTF_8)
            val restored = deserialize(content)
            if (restored.characters.isEmpty()) {
                return OperationResult(false, "Резервная копия не содержит персонажей.")
            }
            writeExternalFile(context, content)
            CharacterRepository(context).replaceCache(restored.characters, restored.lastPage)
            // Файл успешно возвращён во внешнюю папку: операция перемещения завершена.
            internal.delete()
            OperationResult(true, "Файл восстановлен в Downloads/RickAndMortyHub. Персонажей: ${restored.characters.size}.", restored.characters.size)
        } catch (error: Exception) {
            OperationResult(false, "Ошибка восстановления: ${error.localizedMessage ?: error.javaClass.simpleName}")
        }
    }

    fun getInfo(context: Context): BackupInfo {
        val internal = internalBackupFile(context)
        val external = runCatching { externalFileDetails(context) }.getOrNull()
        val internalExists = internal.exists() && internal.length() > 0L
        val description = when {
            external != null -> "Файл: $FILE_NAME\nРасположение: Downloads/RickAndMortyHub\nРазмер: ${formatSize(external.first)}\nИзменён: ${formatDate(external.second)}"
            internalExists -> "Внешний файл отсутствует. Внутренняя резервная копия: ${formatSize(internal.length())}."
            else -> "Файл отсутствует. Создайте резервную копию после загрузки персонажей."
        }
        return BackupInfo(external != null, internalExists, description)
    }

    fun backupExists(context: Context): Boolean = getInfo(context).externalExists

    private data class RestoredData(val characters: List<Character>, val lastPage: Int)

    private fun serialize(characters: List<Character>, lastPage: Int): String =
        JSONObject().apply {
            put("format", "RickAndMortyHub character export")
            put("version", BACKUP_VERSION)
            put("created_at", System.currentTimeMillis())
            put("last_page", lastPage)
            put("characters", JSONArray().apply {
                characters.forEach { character ->
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
        }.toString(2)

    private fun deserialize(content: String): RestoredData {
        val root = JSONObject(content)
        require(root.optInt("version", -1) == BACKUP_VERSION) { "Неподдерживаемая версия файла." }
        val array = root.optJSONArray("characters") ?: throw IOException("В файле отсутствует список персонажей.")
        val characters = ArrayList<Character>(array.length())
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: throw IOException("Некорректная запись персонажа №${index + 1}.")
            require(item.has("id") && item.has("name")) { "У персонажа отсутствуют обязательные поля." }
            characters += Character(
                id = item.getInt("id"),
                name = item.getString("name"),
                status = item.optString("status"),
                species = item.optString("species"),
                gender = item.optString("gender"),
                origin = item.optString("origin")
            )
        }
        return RestoredData(characters.distinctBy { it.id }, root.optInt("last_page", 0))
    }

    private fun internalBackupFile(context: Context): File {
        val directory = File(context.filesDir, "backup")
        if (!directory.exists() && !directory.mkdirs()) throw IOException("Не удалось создать внутреннюю папку резервных копий.")
        return File(directory, FILE_NAME)
    }

    private fun writeExternalFile(context: Context, content: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val existing = findMediaStoreUri(context)
            if (existing != null) resolver.delete(existing, null, null)
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, FILE_NAME)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                put(MediaStore.MediaColumns.RELATIVE_PATH, RELATIVE_DIRECTORY)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val uri = resolver.insert(collection, values) ?: throw IOException("Не удалось создать файл в Downloads.")
            try {
                resolver.openOutputStream(uri, "w")?.bufferedWriter(Charsets.UTF_8)?.use { it.write(content) }
                    ?: throw IOException("Не удалось открыть файл для записи.")
                val ready = ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }
                resolver.update(uri, ready, null, null)
            } catch (error: Exception) {
                resolver.delete(uri, null, null)
                throw error
            }
        } else {
            val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "RickAndMortyHub")
            if (!directory.exists() && !directory.mkdirs()) throw IOException("Не удалось создать папку Downloads/RickAndMortyHub.")
            File(directory, FILE_NAME).writeText(content, Charsets.UTF_8)
        }
    }

    private fun readExternalFile(context: Context): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val uri = findMediaStoreUri(context) ?: return null
            return context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
        }
        val file = legacyExternalFile()
        return if (file.exists()) file.readText(Charsets.UTF_8) else null
    }

    private fun deleteExternalFile(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val uri = findMediaStoreUri(context) ?: return false
            return context.contentResolver.delete(uri, null, null) > 0
        }
        val file = legacyExternalFile()
        return file.exists() && file.delete()
    }

    private fun externalFileDetails(context: Context): Pair<Long, Long>? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val uri = findMediaStoreUri(context) ?: return null
            context.contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.SIZE, MediaStore.MediaColumns.DATE_MODIFIED), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val size = cursor.getLong(0)
                    val modifiedSeconds = cursor.getLong(1)
                    return size to (modifiedSeconds * 1000L)
                }
            }
            return null
        }
        val file = legacyExternalFile()
        return if (file.exists()) file.length() to file.lastModified() else null
    }

    private fun findMediaStoreUri(context: Context): Uri? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val projection = arrayOf(MediaStore.MediaColumns._ID)
        val selection = "${MediaStore.MediaColumns.DISPLAY_NAME}=? AND ${MediaStore.MediaColumns.RELATIVE_PATH}=?"
        context.contentResolver.query(collection, projection, selection, arrayOf(FILE_NAME, RELATIVE_DIRECTORY), null)?.use { cursor ->
            if (cursor.moveToFirst()) return Uri.withAppendedPath(collection, cursor.getLong(0).toString())
        }
        return null
    }

    @Suppress("DEPRECATION")
    private fun legacyExternalFile(): File = File(
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "RickAndMortyHub"),
        FILE_NAME
    )

    private fun formatSize(bytes: Long): String = when {
        bytes < 1024 -> "$bytes Б"
        bytes < 1024 * 1024 -> "%.1f КБ".format(bytes / 1024f)
        else -> "%.1f МБ".format(bytes / (1024f * 1024f))
    }

    private fun formatDate(time: Long): String = DateFormat.getDateTimeInstance().format(Date(time))
}
