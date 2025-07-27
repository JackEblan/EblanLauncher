package com.eblan.launcher.feature.settings.gestures.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.GestureAction

@Composable
fun SelectApplicationDialog(
    modifier: Modifier = Modifier,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    onDismissRequest: () -> Unit,
    onUpdateGestureAction: (GestureAction.OpenApp) -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Select Application")

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(eblanApplicationInfos) { eblanApplicationInfo ->
                        Row(
                            modifier = Modifier
                                .clickable {
                                    val componentName = eblanApplicationInfo.componentName

                                    if (componentName != null) {
                                        onUpdateGestureAction(GestureAction.OpenApp(componentName = componentName))
                                    }

                                    onDismissRequest()
                                }
                                .fillMaxWidth(),
                        ) {
                            AsyncImage(
                                modifier = Modifier.size(40.dp),
                                model = eblanApplicationInfo.icon, contentDescription = null,
                            )

                            Column {
                                Text(text = eblanApplicationInfo.label.toString())

                                Text(
                                    text = eblanApplicationInfo.componentName.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }

                TextButton(
                    modifier = Modifier.align(Alignment.End),
                    onClick = onDismissRequest,
                ) {
                    Text(text = "Cancel")
                }
            }
        }
    }
}