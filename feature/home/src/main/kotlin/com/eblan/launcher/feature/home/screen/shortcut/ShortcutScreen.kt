package com.eblan.launcher.feature.home.screen.shortcut

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.EblanShortcutInfo

@Composable
fun ShortcutScreen(
    modifier: Modifier = Modifier,
    eblanShortcutInfos: Map<EblanApplicationInfo, List<EblanShortcutInfo>>,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            eblanShortcutInfos.isEmpty() -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            else -> {
                LazyColumn {
                    items(eblanShortcutInfos.keys.toList()) { eblanApplicationInfo ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            AsyncImage(
                                model = eblanApplicationInfo.icon,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                            )

                            Text(
                                text = eblanApplicationInfo.label.toString(),
                            )

                            eblanShortcutInfos[eblanApplicationInfo]?.forEach { eblanShortcutInfo ->
                                val preview = eblanShortcutInfo.icon
                                    ?: eblanShortcutInfo.eblanApplicationInfo.icon

                                AsyncImage(
                                    model = preview,
                                    contentDescription = null,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}