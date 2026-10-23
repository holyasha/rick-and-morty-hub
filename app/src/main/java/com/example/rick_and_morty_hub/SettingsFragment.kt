package com.example.rick_and_morty_hub

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun SettingsFragment(
    onSettingsChanged: () -> Unit,
    onShowRestoreDialog: () -> Unit,
    onShowDeleteDialog: () -> Unit,
    onShowBackupCreatedDialog: () -> Unit
) {
    val context = LocalContext.current

    val darkTheme = AppSettings.isDarkTheme(context)
    val fontScale = AppSettings.getFontScale(context)
    val language = AppSettings.getLanguage(context)
    val notifications = AppSettings.isNotificationsEnabled(context)
    val backupExists = BackupManager.backupExists(context)

    var currentDarkTheme by remember(darkTheme) { mutableStateOf(darkTheme) }
    var currentFontScale by remember(fontScale) { mutableFloatStateOf(fontScale) }
    var currentLanguage by remember(language) { mutableStateOf(language) }
    var currentNotifications by remember(notifications) { mutableStateOf(notifications) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.appearance),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.dark_theme),
                color = MaterialTheme.colorScheme.onBackground
            )
            Switch(
                checked = currentDarkTheme,
                onCheckedChange = {
                    currentDarkTheme = it
                    AppSettings.setDarkTheme(context, it)
                    onSettingsChanged()
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.font_size),
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = stringResource(R.string.font_size_value, currentFontScale),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodySmall
        )

        Slider(
            value = currentFontScale,
            onValueChange = { currentFontScale = it },
            onValueChangeFinished = {
                AppSettings.setFontScale(context, currentFontScale)
                onSettingsChanged()
            },
            valueRange = 0.8f..1.4f
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.language),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            RadioButton(
                selected = currentLanguage == "Русский",
                onClick = {
                    currentLanguage = "Русский"
                    AppSettings.setLanguage(context, "Русский")
                    onSettingsChanged()
                }
            )
            Text(
                text = stringResource(R.string.russian),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            RadioButton(
                selected = currentLanguage == "English",
                onClick = {
                    currentLanguage = "English"
                    AppSettings.setLanguage(context, "English")
                    onSettingsChanged()
                }
            )
            Text(
                text = stringResource(R.string.english),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.notifications),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Switch(
            checked = currentNotifications,
            onCheckedChange = {
                currentNotifications = it
                AppSettings.setNotificationsEnabled(context, it)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.backup),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (backupExists)
                stringResource(R.string.backup_exists)
            else
                stringResource(R.string.backup_not_exists),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                BackupManager.createBackup(context)
                onShowBackupCreatedDialog()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.create_backup))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onShowRestoreDialog,
            enabled = backupExists,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.restore_backup))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onShowDeleteDialog,
            enabled = backupExists,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.delete_backup))
        }
    }
}
