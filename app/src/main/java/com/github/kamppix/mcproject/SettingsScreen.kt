package com.github.kamppix.mcproject

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil3.compose.AsyncImage
import com.github.kamppix.mcproject.ui.theme.MCProjectTheme
import java.io.File
import java.io.IOException

@Composable
fun SettingsCategory(
    title: String,
    body: @Composable () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge,
                fontSize = 30.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                body()
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProfileSettings(
    profileName: String,
    profilePicture: String,
    onChangeProfileName: (String) -> Unit,
    onChangeProfilePicture: () -> Unit
) {
    SettingsCategory("Profile") {
        AsyncImage(
            model = profilePicture,
            contentDescription = "Profile picture",
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clickable { onChangeProfilePicture() }
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = "Nickname",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleSmall,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                var isEditing by remember { mutableStateOf(false) }
                var currentName by remember { mutableStateOf(profileName) }

                val textModifier = Modifier.weight(1f, false)
                if (isEditing) {
                    TextField(
                        modifier = textModifier,
                        value = currentName,
                        singleLine = true,
                        onValueChange = { currentName = it },
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            onChangeProfileName(currentName)
                            isEditing = false
                        })
                    )

                } else {
                    Text(
                        modifier = textModifier,
                        text = currentName,
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 22.sp
                    )
                }

                if (isEditing) {
                    IconButton(onClick = {
                        onChangeProfileName(currentName)
                        isEditing = false
                    }) {
                        Icon(Icons.Default.Check, "Save name", tint = MaterialTheme.colorScheme.secondary)
                    }
                } else {
                    IconButton(onClick = { isEditing = true }) {
                        Icon(Icons.Default.Edit, "Edit name", tint = MaterialTheme.colorScheme.secondary)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))
            }
        }
    }
}

@Composable
fun NotificationSettings() {
    val context = LocalContext.current
    val permission = Manifest.permission.POST_NOTIFICATIONS

    var notificationsEnabled by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsEnabled = granted
    }

    SettingsCategory("Notifications") {
        if (notificationsEnabled) {
            Button(onClick = {}, enabled = false) { Text("Notifications enabled") }
        } else {
            Button(onClick = { permissionLauncher.launch(permission) }) { Text("Enable notifications") }
        }
    }
}

@Composable
fun AudioSettings() {
    val context = LocalContext.current
    val permission = Manifest.permission.RECORD_AUDIO
    val audioFilePath = File(context.filesDir, "temp_audio.3gp").absolutePath

    // Initialize variables
    var isRecording by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var recorder: MediaRecorder? = null

    // Permission request
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // Start recording
            isRecording = true
            recorder = MediaRecorder(context).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFilePath)
                try {
                    prepare()
                    start()
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                }
            }
        }
    }

    SettingsCategory("Audio") {
        // Create button based on state
        if (isRecording) {
            Button(onClick = {
                // Stop recording
                isRecording = false
                recorder?.apply {
                    stop()
                    release()
                }

                // Start playback
                isPlaying = true
                MediaPlayer().apply {
                    try {
                        setDataSource(audioFilePath)
                        prepare()
                        start()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    setOnCompletionListener {
                        isPlaying = false
                        release()
                    }
                }
            }) { Text("Stop recording") }
        } else if (isPlaying) {
            Button(onClick = {}, enabled = false) { Text("Playing audio...") }
        } else {
            Button(onClick = { permissionLauncher.launch(permission) }) { Text("Test audio input") }
        }
    }
}

@Composable
fun SettingsScreen(
    profileName: String,
    profilePicture: String,
    onChangeProfileName: (String) -> Unit,
    onChangeProfilePicture: () -> Unit,
    onNavigateBack: () -> Unit
) {
    AppLayout(
        topBarContentLeft = {
            VerticalCenter {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, "Back")
                }
            }
        }
    ) {
        Column {
            ProfileSettings(profileName, profilePicture, onChangeProfileName, onChangeProfilePicture)
            NotificationSettings()
            AudioSettings()
        }
    }
}

@Preview
@Composable
fun PreviewProfileSettings() {
    MCProjectTheme {
        Surface {
            ProfileSettings(
                "Kamppi",
                "",
                onChangeProfileName = {},
                onChangeProfilePicture = {}
            )
        }
    }
}

@Preview
@Composable
fun PreviewNotificationSettings() {
    MCProjectTheme {
        Surface {
            NotificationSettings()
        }
    }
}

@Preview
@Composable
fun PreviewAudioSettings() {
    MCProjectTheme {
        Surface {
            AudioSettings()
        }
    }
}

@Preview(name = "Light Mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Composable
fun PreviewSettingsScreen() {
    MCProjectTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            SettingsScreen(
                "Kamppi",
                "",
                onChangeProfileName = {},
                onChangeProfilePicture = {},
                onNavigateBack = {}
            )
        }
    }
}
