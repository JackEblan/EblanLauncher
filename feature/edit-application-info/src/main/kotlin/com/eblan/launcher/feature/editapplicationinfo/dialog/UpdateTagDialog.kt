package com.eblan.launcher.feature.editapplicationinfo.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.component.EblanDialogContainer
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.EblanApplicationInfoTag
import com.eblan.launcher.domain.model.EblanApplicationInfoTagUi

@Composable
internal fun UpdateTagDialog(
    modifier: Modifier = Modifier,
    eblanApplicationInfoTagUi: EblanApplicationInfoTagUi?,
    onDismissRequest: () -> Unit,
    onUpdateEblanApplicationInfoTag: (EblanApplicationInfoTag) -> Unit,
    onDeleteEblanApplicationInfoTag: (EblanApplicationInfoTag) -> Unit,
) {
    if (eblanApplicationInfoTagUi == null) return

    var value by remember { mutableStateOf(eblanApplicationInfoTagUi.name) }

    var isError by remember { mutableStateOf(false) }

    EblanDialogContainer(onDismissRequest = onDismissRequest) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Update Tag", style = MaterialTheme.typography.titleLarge)

                IconButton(
                    onClick = {
                        onDeleteEblanApplicationInfoTag(
                            EblanApplicationInfoTag(
                                id = eblanApplicationInfoTagUi.id,
                                name = eblanApplicationInfoTagUi.name,
                            ),
                        )
                    },
                ) {
                    Icon(
                        imageVector = EblanLauncherIcons.Delete,
                        contentDescription = null,
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            TextField(
                value = value,
                onValueChange = {
                    value = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = "Update Tag")
                },
                supportingText = {
                    if (isError) {
                        Text(text = "Tag is not valid")
                    }
                },
                isError = isError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true,
            )

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
                        if (value.isNotBlank()) {
                            onUpdateEblanApplicationInfoTag(
                                EblanApplicationInfoTag(
                                    id = eblanApplicationInfoTagUi.id,
                                    name = value,
                                ),
                            )

                            onDismissRequest()
                        } else {
                            isError = true
                        }
                    },
                ) {
                    Text(text = "Update")
                }
            }
        }
    }
}