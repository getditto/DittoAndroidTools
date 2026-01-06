package live.ditto.tools.presencedegradationreporter.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import live.ditto.tools.presencedegradationreporter.theme.PresenceDegradationReporterTheme

@Composable
fun PeersForm(
    expectedPeers: Int,
    reportApiEnabled: Boolean,
    onSave: (expectedPeers: Int, reportApiEnabled: Boolean) -> Unit
) {
    var peers by remember(expectedPeers) { mutableStateOf(expectedPeers) }
    var apiEnabled by remember(reportApiEnabled) { mutableStateOf(reportApiEnabled) }
    var isError by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 16.dp),
        ) {
            Text(
                "Expected minimum number of peers in the mesh:"
            )
            BasicTextField(
                value = peers.toString(),
                onValueChange = {
                    peers = parseNumber(it)
                    isError = false
                },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(16.dp)
            )
            if (isError) {
                Text(
                    text = "Value must be greater than zero.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = apiEnabled,
                    onCheckedChange = { apiEnabled = !apiEnabled }
                )

                Text(
                    text = "Enable Report API"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                shape = RoundedCornerShape(10),
                onClick = {
                    if (peers <= 0) {
                        isError = true
                    } else {
                        onSave(peers, apiEnabled)
                    }
                }
            ) {
                Text(text = "Save")
            }
        }
    }
}

@Preview
@Composable
private fun PeersFormPreview() {
    PresenceDegradationReporterTheme {
        PeersForm(
            expectedPeers = 1,
            reportApiEnabled = true,
            onSave = { _, _ -> }
        )
    }
}

private fun parseNumber(text: String): Int {
    val numberStr = text.filter { it.isDigit() }
    if (numberStr.isEmpty()) return 0

    return numberStr.toInt()
}
