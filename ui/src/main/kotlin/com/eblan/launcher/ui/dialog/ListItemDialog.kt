package com.eblan.launcher.ui.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.eblan.launcher.designsystem.component.EblanDialogContainer

@Composable
fun <T> ListItemDialog(
    modifier: Modifier = Modifier,
    items: List<T>,
    title: String,
    onDismissRequest: () -> Unit,
    onItemSelected: (T) -> Unit,
    label: (T) -> String,
    subtitle: (T) -> String? = { null },
    icon: (T) -> Any? = { null },
) {
    EblanDialogContainer(onDismissRequest = onDismissRequest) {
        Column(modifier = modifier.fillMaxWidth()) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(items) { item ->
                    ListItem(
                        headlineContent = { Text(text = label(item)) },
                        supportingContent = {
                            subtitle(item)?.let { Text(text = it) }
                        },
                        leadingContent = {
                            val iconModel = icon(item)

                            if (iconModel != null) {
                                AsyncImage(
                                    model = iconModel,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                )
                            }
                        },
                        modifier = Modifier
                            .clickable {
                                onItemSelected(item)
                                onDismissRequest()
                            }
                            .fillMaxWidth()
                            .padding(10.dp),
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
