package com.maneo.app.feature.blocker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.maneo.app.core.domain.model.Verse
import com.maneo.app.feature.blocker.domain.Prayer
import com.maneo.app.ui.theme.ManeoTheme

@Composable
fun InterceptScreen(
    verse: Verse,
    prayer: Prayer,
    timerEnabled: Boolean,
    remainingSeconds: Int,
    timerTotalSeconds: Int,
    onWait: () -> Unit,
    onContinue: () -> Unit,
) {
    val buttonsVisible = !timerEnabled || remainingSeconds == 0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = verse.reference,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = verse.text,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(32.dp))

            Text(
                text = prayer.text,
                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(48.dp))

            if (buttonsVisible) {
                Button(
                    onClick = onWait,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text(
                        text = "Amen",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }

                Spacer(Modifier.height(8.dp))

                TextButton(
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Continue anyway",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.semantics {
                        contentDescription = "$remainingSeconds seconds remaining"
                    },
                ) {
                    CircularProgressIndicator(
                        progress = { remainingSeconds.toFloat() / timerTotalSeconds.toFloat() },
                        modifier = Modifier.size(72.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        strokeWidth = 4.dp,
                    )
                    Text(
                        text = "$remainingSeconds",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InterceptScreenPreview() {
    ManeoTheme {
        InterceptScreen(
            verse = Verse(
                id = "v001",
                reference = "Matthew 11:28",
                text = "\"Come to me, all you who labor and are heavily burdened, and I will give you rest.\"",
                slots = listOf("intercept"),
                tone = listOf("inviting"),
            ),
            prayer = Prayer(
                id = "p001",
                text = "Lord, quiet my heart and draw me close to you right now.",
            ),
            timerEnabled = false,
            remainingSeconds = 0,
            timerTotalSeconds = 10,
            onWait = {},
            onContinue = {},
        )
    }
}
