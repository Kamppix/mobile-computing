package com.github.kamppix.mcproject

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import java.io.File

@Composable
fun ProfileSettings() {

    val context = LocalContext.current

    val nameFile = File(context.filesDir, "profileName")
    val profileName by remember {
        mutableStateOf(nameFile.readBytes().decodeToString())
    }

    val pictureFile = File(context.filesDir, "profilePicture")
    var profilePicture by remember {
        mutableStateOf(pictureFile.toUri().toString())
    }

    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val resolver = context.contentResolver
            resolver.openInputStream(uri).use { stream ->
                stream?.copyTo(pictureFile.outputStream())
            }
            profilePicture = pictureFile.toUri().toString() + "?timestamp=${System.currentTimeMillis()}"
        }
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Profile",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge,
                fontSize = 30.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                AsyncImage(
                    model = profilePicture,
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
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
                                    nameFile.writeBytes(currentName.toByteArray())
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
                                nameFile.writeBytes(currentName.toByteArray())
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
    }
}

@Composable
fun SettingsScreen(
    onNavigateToChat: () -> Unit
) {
    AppLayout(
        topBarContentLeft = {
            VerticalCenter {
                IconButton(onClick = onNavigateToChat) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, "Back")
                }
            }
        }
    ) {
        ProfileSettings()
    }
}
