package com.github.kamppix.mcproject

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.github.kamppix.mcproject.ui.theme.MCProjectTheme


data class Message(val author: String, val body: String)

@Composable
fun MessageCard(msg: Message, profilePicture: String? = null) {
    Row(modifier = Modifier.padding(all = 12.dp)) {
        AsyncImage(
            model = profilePicture,
            contentDescription = "User profile picture",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )

        Spacer(modifier = Modifier.width(8.dp))

        var isExpanded by remember { mutableStateOf(false) }
        val surfaceColor by animateColorAsState(
            if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            label = "Expansion color animation"
        )

        Column(modifier = Modifier.clickable { isExpanded = !isExpanded }) {
            Text(
                text = msg.author,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = MaterialTheme.shapes.small,
                shadowElevation = 3.dp,
                color = surfaceColor,
                modifier = Modifier
                    .animateContentSize()
                    .padding(1.dp)
            ) {
                Text(
                    text = msg.body,
                    modifier = Modifier.padding(all = 4.dp),
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun Chat(
    messages: List<Message>,
    profileName: String,
    profilePicture: String
) {
    LazyColumn {
        items(messages) { message ->
            MessageCard(Message(profileName, message.body), profilePicture)
        }
    }
}

@Composable
fun ChatScreen(
    messages: List<Message>,
    profileName: String,
    profilePicture: String,
    onNavigateToSettings: () -> Unit
) {
    AppLayout(
        topBarContentRight = {
            VerticalCenter {
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Default.Settings, "Settings")
                }
            }
        }
    ) {
        Chat(messages, profileName, profilePicture)
    }
}

@Preview
@Composable
fun PreviewMessageCard() {
    MCProjectTheme {
        Surface {
            MessageCard(
                msg = Message("Kamppi", "Hey, take a look at Jetpack Compose, it's great!")
            )
        }
    }
}

@Preview
@Composable
fun PreviewChat() {
    MCProjectTheme {
        Surface {
            Chat(
                SampleData.conversationSample,
                "Kamppi",
                ""
            )
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
fun PreviewChatScreen() {
    MCProjectTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            ChatScreen(
                SampleData.conversationSample,
                "Kamppi",
                "",
                onNavigateToSettings = {}
            )
        }
    }
}
