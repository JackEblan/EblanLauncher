package com.eblan.launcher.feature.home.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.component.EblanDialogContainer
import com.eblan.launcher.designsystem.component.EblanRadioButton
import com.eblan.launcher.domain.model.EblanApplicationInfoOrder
import com.eblan.launcher.ui.settings.SettingsSwitch

@Composable
internal fun EblanApplicationInfoOrderDialog(
    modifier: Modifier = Modifier,
    eblanApplicationInfoOrder: EblanApplicationInfoOrder,
    isRearrangeEblanApplicationInfo: Boolean,
    onDismissRequest: () -> Unit,
    onUpdateClick: (EblanApplicationInfoOrder) -> Unit,
    onUpdateIsRearrangeEblanApplicationInfo: (Boolean) -> Unit,
) {
    var selectedEblanApplicationInfoOrder by remember { mutableStateOf(eblanApplicationInfoOrder) }

    EblanDialogContainer(onDismissRequest = onDismissRequest) {
        Column(
            modifier = modifier
                .selectableGroup()
                .fillMaxWidth(),
        ) {
            Text(
                modifier = Modifier.padding(10.dp),
                text = "Sort Applications",
                style = MaterialTheme.typography.titleLarge,
            )

            EblanApplicationInfoOrder.entries.forEach { eblanApplicationInfoOrder ->
                EblanRadioButton(
                    text = eblanApplicationInfoOrder.name,
                    selected = selectedEblanApplicationInfoOrder == eblanApplicationInfoOrder,
                    onClick = {
                        selectedEblanApplicationInfoOrder = eblanApplicationInfoOrder
                    },
                )
            }

            if (selectedEblanApplicationInfoOrder == EblanApplicationInfoOrder.Custom) {
                SettingsSwitch(
                    checked = isRearrangeEblanApplicationInfo,
                    title = "Rearrange Applications",
                    subtitle = "Rearrange applications by index",
                    onCheckedChange = onUpdateIsRearrangeEblanApplicationInfo,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        end = 10.dp,
                        bottom = 10.dp,
                    ),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismissRequest) {
                    Text(text = "Cancel")
                }
                TextButton(
                    onClick = {
                        onUpdateClick(selectedEblanApplicationInfoOrder)
                    },
                ) {
                    Text(text = "Update")
                }
            }
        }
    }
}
