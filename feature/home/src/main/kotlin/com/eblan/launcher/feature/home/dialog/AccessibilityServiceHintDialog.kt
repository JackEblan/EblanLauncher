package com.eblan.launcher.feature.home.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.component.EblanDialogContainer

@Composable
fun AccessibilityServiceHintDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit
) {
    EblanDialogContainer(onDismissRequest = onDismissRequest) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(10.dp),
        ) {
            Text(text = "Accessibility Service", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(10.dp))

            Text(text = "Accessibility Service needs to be enabled for Eblan Launcher")

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(
                    onClick = onDismissRequest,
                ) {
                    Text(text = "Cancel")
                }
                TextButton(
                    onClick = {

                    },
                ) {
                    Text(text = "Open Settings")
                }
            }
        }
    }
}