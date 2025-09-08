package com.eblan.launcher.feature.settings.gestures.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
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
        Surface(shape = RoundedCornerShape(size = 10.dp)) {
            Column(
                modifier = modifier
                    .fillMaxWidth()
            ) {
                Text(text = "Select Application", style = MaterialTheme.typography.titleLarge)

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(eblanApplicationInfos) { eblanApplicationInfo ->
                        ListItem(
                            headlineContent = { Text(text = eblanApplicationInfo.label.toString()) },
                            supportingContent = { Text(text = eblanApplicationInfo.componentName.toString()) },
                            leadingContent = {
                                AsyncImage(
                                    model = eblanApplicationInfo.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                )
                            },
                            modifier = Modifier
                                .clickable {
                                    val componentName = eblanApplicationInfo.componentName

                                    if (componentName != null) {
                                        onUpdateGestureAction(GestureAction.OpenApp(componentName = componentName))
                                    }

                                    onDismissRequest()
                                }
                                .fillMaxWidth()
                                .padding(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

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